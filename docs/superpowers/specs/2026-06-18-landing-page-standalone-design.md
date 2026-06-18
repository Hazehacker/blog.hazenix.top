# 着陆页独立入口设计（方案 C）

**日期**：2026-06-18
**作者**：Hazenix + Claude
**关联问题**：PSI 性能分卡在 60，Galaxy 优化触顶后瓶颈转移到主包大小

## 背景

经过两轮性能优化（Galaxy WebGL 推迟 + 自动化抓取跳过），PSI 移动端分数从 49 提升到 60，桌面端 60，但仍然无法突破 90 分。Lighthouse 报告显示着陆页加载了大量未使用的 JavaScript（276 KB），定位到根因：

**着陆页 `/` 强制加载了 347 KB 的主应用 bundle**，包含 Vue Router、Pinia stores、Element Plus、所有路由的引用、所有 utils、theme store、OAuth 处理等。在 PSI 4× CPU 节流环境下，仅仅是解析这堆 JavaScript 就需要 20+ 秒，构成 21.7 秒的 TBT。

而着陆页实际只需要 19 KB（Galaxy + LoginDialog）。

## 目标

把 `/` 着陆页从 Vue 主应用中剥离，使用独立的迷你入口，bundle 控制在 ~50 KB 以内。

- **PSI 移动端**：60 → 92-96
- **PSI 桌面端**：60 → 96-100
- **不影响**：登录功能、视觉效果、OAuth 流程
- **样式要求**：与原 LoginDialog 视觉完全一致

## 架构

```
frontend/
├── index.html          # 主应用入口（不变）
├── landing.html        # 【新】着陆页 HTML 入口
├── vite.config.js      # 【改】多入口配置
├── src/
│   ├── main.js         # 主应用 bootstrap（不变）
│   ├── landing.ts      # 【新】着陆页 bootstrap（仅 Vue + Index 组件）
│   ├── views/
│   │   └── index.vue   # 【改】去掉 router/store 依赖
│   └── components/common/
│       ├── LoginDialog.vue        # 主应用版本，保留不动
│       └── LandingLogin.vue       # 【新】着陆页专用，无 Element Plus / Pinia
├── scripts/
│   └── prerender.js    # 【改】/ 路径走 landing 入口
└── public/
    └── ...

服务器：
└── nginx.conf          # 【改】/ 路由指向 landing.html
```

## 详细设计

### 1. landing.html（独立 HTML 入口）

复用 `index.html` 的骨架（preload、preconnect、第三方脚本延迟），但 script 引用 `landing.ts`。骨架屏简化为纯 Galaxy 占位的暗色背景，避免白屏闪烁。

### 2. src/landing.ts（极简 bootstrap）

```ts
import { createApp } from 'vue'
import IndexView from './views/index.vue'
import './style.css'
createApp(IndexView).mount('#app')
```

不引入：Pinia、Vue Router、Element Plus、任何 store、任何 utils。

### 3. views/index.vue 改造

| 原依赖 | 改成 |
|--------|------|
| `useRouter()` → `router.push('/home?fromIndex=true')` | `window.location.href = '/home?fromIndex=true'` |
| `<router-link to="/home">` | `<a href="/home">` |
| `<LoginDialog>` 组件 | `<LandingLogin>` 组件（新建） |

### 4. LandingLogin.vue（视觉等价替代）

视觉要求：与原 `LoginDialog.vue` 像素级一致。

实现策略：
- **CSS 完整复制**：把 `LoginDialog.vue` 的整段 scoped style 原封不动搬过来（包含暗色主题、响应式断点、动画）
- **Element Plus 组件原生替代**：
  | 原 | 替代 |
  |----|------|
  | `<el-form>` + rules | `<form>` + 手写校验 |
  | `<el-input>` | `<input>` 包一层 `.el-input__wrapper` 样式 div |
  | `<el-button :loading>` | `<button>` + spinner span |
  | `ElMessage.success/error/info` | 自制 toast（DOM 注入 + 自动消失）或 alert 兜底 |
- **Vue Router 替代**：
  - `router.push('/register')` → `window.location.href = '/register'`
  - `router.replace('/home')` → `window.location.href = '/home'`
- **Pinia 替代**：
  - `userStore.login(loginForm)` → `fetch('/api/user/user/login', ...)` + `localStorage.setItem('token', ...)`
  - `userStore.getUserInfo()` → `fetch('/api/user/user/userinfo', ...)` + 缓存到 localStorage
  - `userStore.googleLoginByIdToken(idToken)` → `fetch('/api/user/user/google/idtoken-login', ...)`
- **OAuth 流程**：GitHub / Google 按钮直接 `window.location.href` 跳转到后端授权 URL，回调由现有 `/home` 路由的 OAuth callback handler 接管，无需改后端

### 5. vite.config.js 多入口

```js
build: {
  rollupOptions: {
    input: {
      main: path.resolve(__dirname, 'index.html'),
      landing: path.resolve(__dirname, 'landing.html'),
    }
  }
}
```

### 6. Nginx 路由

在 `nginx-blog.conf` 的 server 块顶部加：

```nginx
# 着陆页独立入口：/ 直接服务 landing.html，不走 prerender / SPA fallback
location = / {
  try_files /landing.html =404;
}
```

保留现有 `location /` 处理其他所有路由。

### 7. prerender.js 改动

`/` 这条路径不再走 puppeteer 渲染（因为 landing.html 是真静态 HTML，无需预渲染），从 STATIC_PAGES 列表移除。`rewriteAssetRefs()` 步骤已经能正确处理多入口，无需改动。

## 取舍说明

### 为什么不直接复用 LoginDialog？

复用要带上 Element Plus（~200 KB）+ Pinia（~50 KB）+ store + utils + request 拦截器。bundle 飙升到 250 KB+，节省的工作量约 30 分钟，但**性能优化失败**——白做。

### 为什么登录后用 `window.location.href` 而非 SPA 跳转？

着陆页是无 Vue Router 环境，跳转必须走浏览器导航。但着陆页用户登录后**本来就要离开**着陆页进入主应用，多一次 HTML 请求无所谓——HTML 是预渲染的 5 KB，立即返回。

### 为什么 captcha 还是前端生成？

原 LoginDialog 用的就是前端生成的字符串校验码（非真实图形验证码），仅用于阻挡傻瓜爬虫。继承同样设计，无需引入新依赖。

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| Tailwind 类未被扫描（landing.html / landing.ts 没在 content 配置里） | 检查 `tailwind.config.js`，确保 content glob 覆盖新文件 |
| OAuth 回调期望从主应用发起 | 已确认：回调 URL 落到 `/home`，主应用 Home.vue 的 OAuth handler 接管 |
| 旧 LoginDialog 重复存在 | 保留不动，主应用其他地方可能用到（点 Home.vue 上的登录按钮） |
| 预渲染脚本对 / 处理 | 移除 / 路径，landing.html 已经是真静态 HTML |
| 视觉差异 | CSS 完整复制，原生 `<input>` 包装层匹配 el-input 样式 |

## 工作量估算

| 阶段 | 时间 |
|------|------|
| landing.html + landing.ts + 调通构建 | 10 min |
| LandingLogin.vue 重写（CSS 复制 + 原生组件） | 30 min |
| index.vue 去依赖 | 10 min |
| Vite + Nginx + prerender 配置 | 15 min |
| 部署、PSI 验证、回归 | 15 min |
| **合计** | **~80 min** |

## 验收标准

- [ ] 本地 `npm run build` 成功，dist 中出现 `landing.html` 和独立的 landing JS chunk
- [ ] landing chunk 总大小 ≤ 80 KB（gzip 后 ≤ 25 KB）
- [ ] 浏览器访问 `/` 看到完整星空 + 登录弹窗 + 按钮，视觉与原版一致
- [ ] 点"登录"→邮箱表单可提交，正确流转到 `/home`
- [ ] 点 GitHub / Google → 跳转 OAuth → 回到 `/home` 并完成登录
- [ ] PSI 重新测试，移动端 ≥ 90 分，桌面端 ≥ 95 分
- [ ] 主应用其他路由不受影响（`/home`、`/articles` 等照常工作）

## 实施分支

主分支 `main` 直接改，不开 worktree——单人项目，改动隔离在 `landing.*` 新文件 + 少量改动 index.vue / nginx，回滚成本低。
