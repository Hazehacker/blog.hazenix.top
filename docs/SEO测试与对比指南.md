# SEO 测试与对比指南

> Google Search Console 数据延迟 1-2 周才显示，但以下方法可以**立即**验证 SEO 效果。所有方法均可在本地/开发环境运行，不需要等待搜索引擎收录。

---

## 一、即时测试方法（部署前就能跑）

### 1.1 Lighthouse（最全面，推荐首选）

**一行命令，30 秒出 SEO 分数：**

```bash
# Chrome DevTools 内置方式
# F12 → Lighthouse 标签 → 勾选 SEO → Generate report

# CLI 方式（可脚本化、可对比）
npx lighthouse https://blog.hazenix.top --only-categories=seo --output=json --output-path=./seo-report.json
npx lighthouse https://blog.hazenix.top --only-categories=seo --output=html --output-path=./seo-report.html
```

**输出解读**：分数 0-100，以及每项检查的通过/失败清单。

| 检查项 | 你当前的预期 |
|--------|-------------|
| `has-viewport` | ✅ 已有 `<meta name="viewport">` |
| `document-title` | ✅ 每个页面动态设置 |
| `meta-description` | ✅ 文章页已接入 setSEO() |
| `http-status-code` | ⚠️ 404 页返回 200（SPA 通病） |
| `is-crawlable` | ✅ robots.txt 未屏蔽 |
| `robots-txt-valid` | ✅ 已添加 |
| `canonical` | ✅ 文章页已设置 |
| `structured-data` | ✅ Article + BreadcrumbList |
| `hreflang` | ⚠️ 未设置（中文站可选） |

### 1.2 结构化数据验证（最关键，立即出结果）

**Google 官方工具**：https://search.google.com/test/rich-results

- 输入你的文章 URL → 立即看到能否触发 Rich Snippet
- 支持的富结果类型：**Article, BreadcrumbList, FAQ, HowTo**

**Schema.org 通用验证**：https://validator.schema.org/

- 更全面，不仅检测 Rich Result，还检测所有 Schema 类型的语法正确性

**本地测试**（部署前）：
```bash
# 1. 启动本地 dev server
cd frontend && npm run dev

# 2. 打开浏览器访问 http://localhost:5173/article/任意文章

# 3. F12 → Elements → 搜索 "application/ld+json"
# 找到类似这样的 script 标签：
# <script type="application/ld+json">{"@context":"https://schema.org",...}</script>

# 4. 复制内容粘贴到 https://validator.schema.org/ 验证
```

### 1.3 移动端友好测试

https://search.google.com/test/mobile-friendly

立即告诉你：
- 文字是否太小
- 点击元素是否太近
- 视口是否正确设置
- 是否有横向滚动条

### 1.4 PageSpeed Insights

https://pagespeed.web.dev/

- 输入 URL → 立即给 Lab Data（模拟数据）
- 同时展示 SEO、Accessibility、Best Practices 分数
- 如果该 URL 已有足够真实用户访问，还会显示 Field Data（CrUX）

### 1.5 curl 模拟爬虫视角

```bash
# 看爬虫第一波抓取时看到什么
curl -s https://blog.hazenix.top | head -30

# 对 SPA 来说，你会看到 <div id="app"></div> 空壳
# 这就是为什么需要 SEO 优化的原因

# 检查 HTTP 响应头
curl -I https://blog.hazenix.top
# 看是否有：Content-Type, X-Robots-Tag, 重定向状态码等
```

### 1.6 Google Search Console → URL 检查（实时）

虽然性能数据要等一周，但 **URL 检查工具的"测试实际网址"是实时的**：

1. 打开 [Google Search Console](https://search.google.com/search-console)
2. 顶部输入框粘贴你的文章 URL → 回车
3. 点击 **"测试实际网址"**（Test Live URL）
4. 立即看到：
   - ✅ 页面是否可索引
   - ✅ Google 看到的渲染后 HTML 截图
   - ✅ 抓取到的资源（哪些被 blocked）
   - ✅ 结构化数据是否被检测到

**这是验证 SEO 改动是否生效最快的官方方法。**

---

## 二、优化前后对比方法

### 2.1 Lighthouse JSON 对比（最精确）

**优化前**：
```bash
npx lighthouse https://blog.hazenix.top \
  --only-categories=seo \
  --output=json \
  --output-path=./seo-before.json
```

**优化后**：
```bash
npx lighthouse https://blog.hazenix.top \
  --only-categories=seo \
  --output=json \
  --output-path=./seo-after.json
```

**对比脚本**（提取关键分数和通过项）：
```bash
# 提取 SEO 分数对比
echo "优化前: $(node -e "console.log(JSON.parse(require('fs').readFileSync('seo-before.json','utf8')).categories.seo.score*100)")分"
echo "优化后: $(node -e "console.log(JSON.parse(require('fs').readFileSync('seo-after.json','utf8')).categories.seo.score*100)")分"

# 列出失败项对比
node -e "
const before = JSON.parse(require('fs').readFileSync('seo-before.json','utf8'));
const after = JSON.parse(require('fs').readFileSync('seo-after.json','utf8'));
const getFails = r => r.categories.seo.auditRefs
  .filter(a => r.audits[a.id].score === 0)
  .map(a => a.id);
console.log('优化前失败:', getFails(before));
console.log('优化后失败:', getFails(after));
"
```

### 2.2 页面源码快照对比

```bash
# 优化前保存源码
curl -s https://blog.hazenix.top > /tmp/seo-before.html

# 优化后保存源码
curl -s https://blog.hazenix.top > /tmp/seo-after.html

# 对比差异（重点关注 <head> 区域）
diff /tmp/seo-before.html /tmp/seo-after.html
```

注意：SPA 的 `curl` 只能看到 HTML 骨架，真正的 meta 标签是 JS 动态注入的。所以对于你这项目，更有效的是：

```bash
# 使用 headless browser 获取渲染后的 HTML
npx puppeteer --url=https://blog.hazenix.top --selector=head --output=/tmp/head-after.html
```

或者简单方式——浏览器打开页面 → 右键查看网页源代码（Ctrl+U）→ 保存 → 对比。

### 2.3 结构化数据检查清单

部署前后，用这张表逐项勾对：

| 检查项 | 优化前 | 优化后 | 验证工具 |
|--------|--------|--------|----------|
| 首页有 WebSite Schema | ❌ | ✅ | Rich Results Test |
| 文章页有 Article Schema | ❌ | ✅ | Rich Results Test |
| 文章页有 BreadcrumbList Schema | ❌ | ✅ | Rich Results Test |
| 文章页有 `<meta name="description">` | ❌ | ✅ | 查看源码 |
| 文章页有 `og:title` / `og:image` | ❌ | ✅ | 查看源码 |
| 文章页有 `<link rel="canonical">` | ❌ | ✅ | 查看源码 |
| SEO 分数（Lighthouse） | ~50-60 | ~90+ | Lighthouse |
| robots.txt 可访问 | ❌ | ✅ | `curl /robots.txt` |
| Sitemap 端点有数据 | ❌ | ✅ | `curl /api/user/articles/slugs` |

### 2.4 Google Search Console 长期对比

虽然数据延迟，但优化前就该注册：

1. **注册 GSC** → 验证域名 → 提交 sitemap
2. 然后关注这些指标的变化趋势（按周对比）：

| 指标 | 路径 | 优化后预期 |
|------|------|-----------|
| 已索引页面数 | 索引 → 页面 | 逐步上升 |
| 平均排名 | 效果 → 搜索结果 | 关键词排名提升 |
| 总点击次数 | 效果 → 搜索结果 | 自然流量增长 |
| 平均 CTR | 效果 → 搜索结果 | 因为有 meta description 和 Rich Snippet 而提升 |
| Core Web Vitals | 体验 → 核心网页指标 | LCP/INP/CLS 达标 |
| 结构化数据 | 增强功能 | Article/Breadcrumb 出现在报告中 |

---

## 三、本地开发环境完整测试流程

```bash
# === 1. 启动服务 ===
# 终端1：后端
cd backend/blog-server && mvn spring-boot:run

# 终端2：前端
cd frontend && npm run dev

# === 2. 验证后端 SEO 端点 ===
# robots.txt
curl http://localhost:9090/robots.txt
# 应返回：User-agent: *\nAllow: /...

# sitemap 数据
curl http://localhost:9090/user/articles/slugs
# 应返回 JSON 数组：[{"id":1,"slug":"xxx","updateTime":"..."}]

# === 3. 验证前端 SEO ===
# 打开 http://localhost:5173
# F12 → 检查 Elements 面板

# 首页检查：
# <head> 中应有：
#   <meta name="description">
#   <script type="application/ld+json"> (含 WebSite + SearchAction)

# 文章页检查（打开任意文章）：
# <head> 中应有：
#   <meta name="description">
#   <meta property="og:title">
#   <meta property="og:image">
#   <meta name="twitter:card">
#   <link rel="canonical">
#   <script type="application/ld+json"> (含 Article + BreadcrumbList)

# === 4. Lighthouse 审计 ===
# Chrome DevTools → Lighthouse → SEO → Generate Report
# 目标分数：≥ 90

# === 5. 结构化数据验证 ===
# 复制 <script type="application/ld+json"> 的内容
# 粘贴到 https://validator.schema.org/

# === 6. Rich Results 验证 ===
# 部署后访问 https://search.google.com/test/rich-results
# 输入生产环境文章 URL
```

---

## 四、快速验证清单（每次 SEO 改动后跑一遍）

```
□ 1. Lighthouse SEO 分数 ≥ 90？
□ 2. robots.txt 可访问？（curl /robots.txt）
□ 3. Sitemap 端点返回数据？（curl /api/user/articles/slugs）
□ 4. 文章页 <head> 有 meta description？
□ 5. 文章页 <head> 有 og:title, og:image？
□ 6. 文章页 <head> 有 <script type="application/ld+json">？
□ 7. Rich Results Test 检测到 Article Schema？
□ 8. Rich Results Test 检测到 BreadcrumbList Schema？
□ 9. Mobile-Friendly Test 通过？
□ 10. 首页有 WebSite Schema（含 SearchAction）？
```

---

## 五、工具速查

| 工具 | 用途 | 延迟 | 链接 |
|------|------|------|------|
| **Lighthouse** | 综合 SEO 评分 | 即时 | Chrome DevTools 内置 |
| **Rich Results Test** | 结构化数据是否能触发富结果 | 即时 | search.google.com/test/rich-results |
| **Schema Validator** | JSON-LD 语法和类型验证 | 即时 | validator.schema.org |
| **Mobile-Friendly Test** | 移动端适配 | 即时 | search.google.com/test/mobile-friendly |
| **PageSpeed Insights** | 性能 + SEO 评分 | 即时 | pagespeed.web.dev |
| **GSC URL Inspection** | Google 看到的渲染结果 | 即时 | search.google.com/search-console |
| **curl + 查看源码** | 爬虫视角的原始 HTML | 即时 | 终端 |
| **Google Search Console** | 搜索性能、索引、排名 | 1-2 周 | search.google.com/search-console |
| **Google Analytics 4** | 流量、用户行为 | 24-48h | analytics.google.com |
