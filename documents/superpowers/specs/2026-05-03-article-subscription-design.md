# 文章订阅 + 首页三按钮 — 设计文档

- 日期：2026-05-03（更新：2026-06-06）
- 范围：首页底部三按钮（喜欢/订阅/催更）+ 文章订阅邮件系统（无确认流程 + /feed 方式）

---

## 0. 变更日志（2026-06-06 深度审查与修复）

### 新增功能

**RSS Feed 实现**（`FeedController.java`）
- 新增 `GET /feed` 端点，返回 `application/rss+xml`，无需鉴权
- 查询最新 20 篇已发布文章（`status=0`，按 `create_time desc`）
- Item link 优先使用 `slug`，无 slug 则用 `id`
- Description 字段：剥离 HTML 标签后取前 300 字
- 日期格式：RFC 822（符合 RSS 2.0 规范），时区 `Asia/Shanghai`
- `atom:link` self-referencing 指向生产域名

**前端订阅弹窗恢复 RSS 入口**（`SiteActionButtons.vue`）
- 恢复"或使用 RSS 阅读器"分隔线
- 新增 feedUrl 输入框（只读）+ 复制按钮 + 打开按钮
- `feedUrl` = `${VITE_API_BASE_URL}/feed`（开发/生产均适配）

### Bug 修复

| # | 文件 | 根本原因 | 修复方案 |
|---|---|---|---|
| 1 | `BlogMailSender.java` | `getRawConfig()` 可能返回 null，之后直接访问 `.getSmtpHost()` 导致 NPE；异常在上层被 catch 吃掉，邮件静默失败 | `send()` 开头加 null 检查，抛出明确 RuntimeException |
| 2 | `ArticleSubscriptionServiceImpl.java` | 用户退订后再次订阅：`existing.status==2` 不进入已订阅 throw，但 `mapper.insert()` 触发 UNIQUE email 约束异常，前端收到 400 "订阅失败" | 检测到 status==2 时改为 `mapper.resubscribe()`（UPDATE），同时刷新 token 和 subscribe_at |
| 3 | `ArticleSubscriptionMapper.java` + XML | 缺少 resubscribe 方法 | 新增 `resubscribe(@Param email, token, subscribeAt)` 及对应 XML UPDATE |
| 4 | `ArticleServiceImpl.java` | `updateArticle()` 在每次保存 `status=0` 时都触发邮件通知，包括对已发布文章的修改，导致订阅者被重复轰炸 | 读取旧 status，仅在 `旧status!=0 && 新status==0` 时通知（真正的首次发布） |
| 5 | `ArticleNotifyServiceImpl.java` | `article.getContent()` 是 WangEditor 输出的原始 HTML，传入 `ArticleMailRenderer.render()` 后 `escapeHtml()` 将 `<p>` 转为 `&lt;p&gt;`，邮件预览显示乱码 | 发送前 `replaceAll("<[^>]+>", "")` 剥离 HTML 标签，再传入 renderer |
| 6 | `ArticleSubscriptionController.java` | 无邮箱格式校验，空值或非法邮箱直接存入数据库 | 正则 `^[^@\s]+@[^@\s]+\.[^@\s]+$` 前置校验，失败返回 `400 邮箱格式错误` |

### 架构说明

**邮件发送依赖链**（调用顺序）：
```
ArticleServiceImpl.updateArticle()
  → ArticleNotifyServiceImpl.notifySubscribers() [Async]
      → ArticleSubscriptionMapper.listActive()
      → ArticleMailRenderer.render(title, plainSummary, id, token)
      → BlogMailSender.send(to, subject, html)
          → NotifyConfigService.getRawConfig()  ← 需 notify_config 表有记录
          → AesCryptoUtil.decrypt(password)    ← 需 blog.notify.encrypt-key 配置
          → JavaMailSenderImpl.send()          ← 需 SMTP 可达
```

**邮件系统前置条件**（任一不满足则静默失败，日志可见）：
1. `notify_config` 表有 id=1 的行（`enabled` 字段当前不影响文章订阅，仅影响日报）
2. SMTP 账号密码正确，服务器可达
3. `application-test.yml` / `application-prod.yml` 中 `blog.notify.encrypt-key` 已配置

**再订阅流程**（修复后）：
```
POST /user/subscription/subscribe { email }
  ├── email 格式校验（新增）
  ├── 查 article_subscription WHERE email = ?
  │   ├── null    → INSERT（新用户首次订阅）
  │   ├── status=1 → 抛 "该邮箱已订阅" → 409
  │   └── status=2 → UPDATE 刷新 token + subscribe_at → 200（修复退订再订）
  └── return 200
```

---

## 1. 现状

- 首页 `Home.vue` 有文章列表 + 侧边栏，无任何互动按钮
- 后端无文章订阅相关表/接口/Servic
- 项目已有 `/feed`（RSS/Atom），前端暂无订阅入口
- "催更"无任何现有数据支撑

---

## 2. 目标

### 首页三按钮

- 喜欢本站：localStorage 标记，只可点一次，toast 感谢语（计算次数，次数会进行存储）
- 订阅文章：弹窗输入邮箱 → 直接订阅（无需确认） + 显示 `/feed` 链接供复制
- 催更：点击 → 后端记录次数 → toast "本月已有 X 人催更，快马加鞭更新中！"

### 文章订阅邮件

- 用户输入邮箱立即订阅（无确认流程）
- 同时提供 `/feed` 作为 RSS 订阅替代方案
- 文章发布（`status: 2→0`）时，给所有已激活订阅者发邮件
- 邮件内容：标题 + 摘要（120 字截断）+ 链接
- 退订：邮件底部退订链接（一次性 token）

---

## 3. 关键决策

| 项 | 取值 | 说明 |
|---|---|---|
| 订阅方式 | 邮箱直订（无确认） | 无需邮箱验证，最简流程 |
| 退订 | 邮件底部退订链接 | 一次性 token，7 天有效 |
| 催更计数 | 后端记录当月次数 | 每月 1 日重置 |
| 喜欢计数 | localStorage | 不打后端，刷新页面不累计 |
| /feed | 现有 `/feed` 路由 | 直接在弹窗中展示 URL 供复制 |

---

## 4. 数据库

### article_subscription 表

```sql
CREATE TABLE article_subscription (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  email        VARCHAR(255) NOT NULL UNIQUE,
  unsubscribe_token VARCHAR(64) NOT NULL UNIQUE,
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=已激活 2=已退订',
  subscribe_at DATETIME     NOT NULL,
  unsubscribe_at DATETIME,
  KEY idx_email (email),
  KEY idx_unsubscribe_token (unsubscribe_token)
);
```

### article_urge 表

```sql
CREATE TABLE article_urge (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  urge_month  VARCHAR(7)  NOT NULL COMMENT 'YYYY-MM',
  count       INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_month (urge_month)
);
```

### site_like 表（喜欢本站）

```sql
CREATE TABLE site_like (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  ip_hash     VARCHAR(64)  NOT NULL COMMENT 'IP 哈希值，防刷量',
  created_at  DATETIME     NOT NULL,
  UNIQUE KEY uk_ip (ip_hash)
);
```

初始化：插入当月记录（urge）；site_like 无需初始化。

---

## 4.1 后台管理面板

均挂在 `/admin/**`，走已有 admin JWT 拦截器。

**订阅管理：**
```
GET  /admin/subscription/list?page=&size=
     → 分页 { id, email, subscribeAt, status }
     → 可删除（soft delete）或批量退订

DELETE /admin/subscription/{id}
     → 物理删除记录

POST  /admin/subscription/export
     → 导出邮箱列表 CSV
```

**催更统计：**
```
GET  /admin/urge/stats
     → 返回各月催更次数列表（含当月）
```

**喜欢统计：**
```
GET  /admin/site-like/stats
     → { totalCount, todayCount, ipList（含分页） }
```

---

## 5. 首页三按钮

### 5.1 组件结构

`frontend/src/components/common/SiteActionButtons.vue`

位置：`Home.vue` 内容区域最底部（最新文章列表之后、lg:col-span-1 侧边栏之前），但视觉上在整页最底部。

插入位置：在 `Home.vue` 的 `<div class="mt-24 grid...">` 区块**之后**插入 `<SiteActionButtons />`。

### 5.2 视觉规格

```
┌──────────────┬──────────────┬──────────────┐
│  ❤️ 喜欢本站  │  ✉️ 订阅文章  │  ⚡ 催更     │
│     42       │              │     31      │
└──────────────┴──────────────┴──────────────┘
```

- 三按钮等宽，flex-1，高度 72px
- 胶囊按钮：圆角 36px，`bg-gray-100 dark:bg-gray-800`，hover 变深
- 主文字：16px，粗体（`font-bold`）
- 数字：13px，次级灰（`text-gray-400 dark:text-gray-500`）
- 图标：Heroicons outline，24px，左侧
- 移动端：保持横向，最小高度 60px

### 5.3 交互行为

**喜欢本站：**
1. 检查 `localStorage.getItem('site_liked')`
2. 若已有标记 → toast "你已喜欢过啦！" → 退出
3. 若无 → 获取 IP（前端无法直接获取，用请求后端接口代替） → `POST /user/site-like` 记录到数据库 → `localStorage.setItem('site_liked', '1')` → 后端返回总数 → 前端显示 → toast "感谢你的支持！"
4. 按钮变为 disabled 状态 + 文字改为"已喜欢"

**订阅文章：**
1. 弹出 `el-dialog`（标题"订阅文章更新"）
2. Dialog 内容：
   - 描述文字："留下邮箱，新文章发布第一时间通知你"
   - 邮箱输入框 + 订阅按钮
   - 分隔线 "— 或 —"
   - "/feed 订阅链接"文字 + 复制按钮（点击复制 `/feed` URL）
3. 点订阅 → 调 `POST /user/subscription/subscribe {email}` → 成功 → toast "订阅成功！" → dialog 关闭
4. 已在订阅列表 → toast "你已订阅，首篇文章发布时通知你"

**催更：**
1. 点催更 → 调 `POST /user/urge` → 返回当月催更总数 `currentCount`
2. toast `"本月已有 ${currentCount} 人催更，快马加鞭更新中！"`（3 秒）
3. 每次点击都有效

### 5.4 前端 API

```js
// src/api/frontend.js 新增
subscribeArticle(data) {
  return request.post('/user/subscription/subscribe', data)
},
urgeArticle() {
  return request.post('/user/urge')
},
likeSite() {
  return request.post('/user/site-like')
}
```

### 5.5 后端喜欢接口

```
POST /user/site-like
  → 200 → { totalCount: 42 }
  → 409 → { message: "已喜欢过" }
```

---

## 6. 后端 API

### 订阅接口

```
POST /user/subscription/subscribe
  body: { email }
  200 → { message: "订阅成功" }
  409 → { message: "该邮箱已订阅" }
  400 → { message: "邮箱格式错误" }
```

```
GET /user/subscription/unsubscribe?token=xxx
  → 返回简单 HTML 页面 "退订成功"
  → status=2
```

### 催更接口

```
POST /user/urge
  200 → { currentCount: 42 }
```

### 文章发布触发

在 `ArticleServiceImpl.createArticle()` 或 `updateArticle()` 中，当 `status` 从 `2→0` 时：
1. 查询 `article_subscription WHERE status=1`
2. 遍历发送邮件（可异步，不阻塞发布流程）
3. 记录 `article_notify_log`

---

## 7. 邮件内容

**主题：** `【Hazenix Blog】新文章：《标题》`

**正文 HTML：**

```html
<h2>《文章标题》</h2>
<p>摘要文字（截断120字）…</p>
<p><a href="https://blog.hazenix.top/article/{id}">阅读全文 →</a></p>
<hr>
<p style="font-size:12px;color:#999">
  <a href="https://blog.hazenix.top/feed">RSS 订阅</a> |
  <a href="https://blog.hazenix.top/api/unsubscribe?token=xxx">退订</a>
</p>
```

---

## 8. 新增 / 修改文件清单

| 组件 | 路径 | 类型 |
|---|---|---|
| 三按钮组件 | `frontend/src/components/common/SiteActionButtons.vue` | 新增 |
| 首页入口 | `frontend/src/views/Home.vue` | 修改：加 `<SiteActionButtons />` |
| 前端 API | `frontend/src/api/frontend.js` | 修改：加 subscribeArticle, urgeArticle, likeSite |
| 订阅实体 | `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleSubscription.java` | 新增 |
| 催更实体 | `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleUrge.java` | 新增 |
| 喜欢实体 | `backend/blog-pojo/src/main/java/top/hazenix/entity/SiteLike.java` | 新增 |
| 发送日志实体 | `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleNotifyLog.java` | 新增 |
| 订阅 Mapper | `backend/blog-server/.../mapper/ArticleSubscriptionMapper.java` (+ xml) | 新增 |
| 催更 Mapper | `backend/blog-server/.../mapper/ArticleUrgeMapper.java` (+ xml) | 新增 |
| 喜欢 Mapper | `backend/blog-server/.../mapper/SiteLikeMapper.java` (+ xml) | 新增 |
| 订阅 Service | `backend/blog-server/.../service/ArticleSubscriptionService.java` (+ Impl) | 新增 |
| 催更 Service | `backend/blog-server/.../service/ArticleUrgeService.java` (+ Impl) | 新增 |
| 喜欢 Service | `backend/blog-server/.../service/SiteLikeService.java` (+ Impl) | 新增 |
| 订阅 Controller | `backend/blog-server/.../controller/user/ArticleSubscriptionController.java` | 新增 |
| 催更 Controller | `backend/blog-server/.../controller/user/ArticleUrgeController.java` | 新增 |
| 喜欢 Controller | `backend/blog-server/.../controller/user/SiteLikeController.java` | 新增 |
| 退订 Controller | `backend/blog-server/.../controller/web/UnsubscribeController.java` | 新增 |
| 后台管理 Controller | `backend/blog-server/.../controller/admin/SubscriptionAdminController.java` | 新增 |
| 后台管理 Controller | `backend/blog-server/.../controller/admin/UrgeAdminController.java` | 新增 |
| 后台管理 Controller | `backend/blog-server/.../controller/admin/SiteLikeAdminController.java` | 新增 |
| 公开退订接口 | `SecurityConfig` | 修改：放行 `/api/unsubscribe` |
| 文章 Service | `ArticleServiceImpl.java` | 修改：发布时触发订阅邮件 |
| SQL | `documents/sql/article_subscription.sql` | 新增 |

---

## 9. 验证清单

- [ ] 三按钮渲染正常，hover 效果正确
- [ ] "喜欢本站"点一次后 disabled，刷新页面保持 disabled，后端记录 +1
- [ ] "订阅文章"弹窗可输入邮箱并订阅成功
- [ ] 订阅弹窗显示 /feed URL，复制按钮有效
- [ ] "催更"toast 显示当月人数，后端累计 +1
- [ ] 后端收到订阅请求写入数据库
- [ ] 文章发布后订阅者收到邮件
- [ ] 邮件底部退订链接可退订
- [ ] 同一邮箱重复订阅返回 409
- [ ] 后台：订阅列表可分页查看 / 删除 / 导出
- [ ] 后台：催更统计可按月查看
- [ ] 后台：喜欢统计可查看总数 / 今日数 / IP 列表
