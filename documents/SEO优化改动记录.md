# SEO 优化改动记录

> 日期：2026-06-16 | 基于 `documents/SEO优化指南.md` 第 11 章审计结果实施

---

## 改动概览

- 新建文件：3 个
- 修改文件：11 个
- 修复审计问题：8 个（5 个严重 + 3 个中等）

---

## 一、后端改动

### 1.1 新建 `ArticleSlugVO.java`

**路径**：`backend/blog-pojo/src/main/java/top/hazenix/vo/ArticleSlugVO.java`

Sitemap 用的轻量 DTO，只含三个字段：

```java
public class ArticleSlugVO {
    private Long id;
    private String slug;
    private LocalDateTime updateTime;
}
```

### 1.2 Mapper 层新增查询

**路径**：
- `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`
- `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`

新增 `getPublishedSlugs()` 方法，SQL 为：

```sql
SELECT id, slug, update_time FROM article WHERE status = 0 ORDER BY update_time DESC
```

只查已发布文章（status=0），按更新时间倒序，用于动态生成 sitemap。

### 1.3 Service 层新增方法

**路径**：
- `backend/blog-server/src/main/java/top/hazenix/service/ArticleService.java`
- `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java`

```java
List<ArticleSlugVO> getPublishedArticleSlugs();
```

实现直接委托 mapper。

### 1.4 Controller 修复两个端点

**路径**：`backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java`

**① `/user/articles/slugs` — 从 TODO 占位改为真实实现**

修复前：
```java
@GetMapping("/slugs")
public Result getArticlesSlugList() {
    log.info("获取文章slug列表");
    //TODO 后期做
    return Result.success();  // 返回空数据
}
```

修复后：
```java
@GetMapping("/slugs")
public Result<List<ArticleSlugVO>> getArticlesSlugList() {
    log.info("获取文章slug列表");
    List<ArticleSlugVO> slugs = articleService.getPublishedArticleSlugs();
    return Result.success(slugs);
}
```

**② 新增 `/robots.txt` 端点**

```java
@GetMapping(value = "/robots.txt", produces = "text/plain")
public String getRobotsTxt() {
    return "User-agent: *\n"
         + "Allow: /\n"
         + "Disallow: /admin/\n"
         + "Disallow: /api/\n"
         + "Disallow: /profile\n"
         + "Disallow: /register\n"
         + "Sitemap: https://blog.hazenix.top/sitemap.xml\n";
}
```

> 注意：后端 `WebMvcConfiguration` 继承 `WebMvcConfigurationSupport` 禁用了 Spring Boot 默认静态资源处理，所以 robots.txt 通过 Controller 端点返回而非静态文件。

---

## 二、前端改动

### 2.1 新建 `robots.txt`

**路径**：`frontend/public/robots.txt`

Vite 构建时自动复制到 `dist/`。内容与后端端点一致但通过 Nginx 直接提供静态文件。

### 2.2 修复 `index.html`

**路径**：`frontend/index.html`

| 行 | 修复前 | 修复后 | 原因 |
|----|--------|--------|------|
| `<html lang="">` | 空值 | `lang="zh-CN"` | 无障碍 + 搜索引擎语言判断 |
| 缺少 meta description | 无 | 添加 `<meta name="description" content="...">` | 搜索结果摘要 |
| Font Awesome CDN | 同步加载（阻塞渲染） | `media="print" onload="this.media='all'"` | 消除渲染阻塞 |

### 2.3 修复 `seo.js` 默认站点名

**路径**：`frontend/src/utils/seo.js`

四处默认值从 `'Vue Blog'` 改为 `'Hazenix的后端札记'`：

- `setPageTitle()` 默认 siteName
- `generateWebsiteStructuredData()` 默认 name
- `setSEO()` 默认 siteName
- `generateArticleStructuredData()` 中 publisher.name

### 2.4 ArticleDetail.vue — SEO 元数据（最重要）

**路径**：`frontend/src/views/ArticleDetail.vue`

**修复前**：整篇文章页面只设置了一行 `document.title`，没有 meta description、OG 标签、结构化数据。

**修复后**：在 `loadArticle()` 文章加载成功后调用 `setSEO()`，包含：

| 设置的标签 | 来源数据 |
|-----------|----------|
| `<title>` | `article.title` |
| `<meta name="description">` | `article.summary` 或正文前 160 字 |
| `<meta name="keywords">` | `article.tags` 提取 |
| `<meta name="author">` | `article.authorName` |
| `<meta property="og:title">` | 同上 |
| `<meta property="og:description">` | 同上 |
| `<meta property="og:image">` | `article.coverImage` |
| `<meta property="og:url">` | 当前页面 URL |
| `<meta property="og:type">` | `article` |
| `<meta name="twitter:card">` | `summary_large_image` |
| `<link rel="canonical">` | 当前页面 URL |
| `<script type="application/ld+json">` | **Article** + **BreadcrumbList** 结构化数据 |

结构化数据示例：
```json
{
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "Article",
      "headline": "文章标题",
      "author": { "@type": "Person", "name": "Hazenix" },
      "publisher": { "@type": "Organization", "name": "Hazenix的后端札记" },
      "datePublished": "2026-06-15T10:00:00+08:00",
      "dateModified": "2026-06-15T14:00:00+08:00"
    },
    {
      "@type": "BreadcrumbList",
      "itemListElement": [
        { "position": 1, "name": "首页", "item": "https://..." },
        { "position": 2, "name": "文章", "item": "https://..." },
        { "position": 3, "name": "分类名", "item": "https://..." },
        { "position": 4, "name": "文章标题" }
      ]
    }
  ]
}
```

### 2.5 Home.vue — 首页 SEO + WebSite 结构化数据

**路径**：`frontend/src/views/Home.vue`

在 `onMounted` 中设置：

- 页面标题、描述
- **WebSite** 结构化数据（含 `SearchAction`），用于 Google Sitelinks Searchbox

### 2.6 CategoryDetail.vue / TagDetail.vue / CategoryList.vue / TagList.vue

**路径**：
- `frontend/src/views/CategoryDetail.vue`
- `frontend/src/views/TagDetail.vue`
- `frontend/src/views/CategoryList.vue`
- `frontend/src/views/TagList.vue`

均在数据加载成功后调用 `setSEO()`，设置页面标题和描述。

---

## 三、修复对照表

| 审计编号 | 问题 | 严重程度 | 状态 |
|----------|------|----------|------|
| #1 | robots.txt 缺失 | 🔴 严重 | ✅ 已修复 |
| #2 | XML Sitemap 缺失 | 🔴 严重 | ✅ 已修复（后端 `/slugs` 端点实现） |
| #4 | ArticleDetail SEO 元数据严重不足 | 🔴 严重 | ✅ 已修复 |
| #5 | 结构化数据从未使用 | 🔴 严重 | ✅ 已修复 |
| #6 | `<html lang="">` 空值 | 🟡 中等 | ✅ 已修复 |
| #8 | index.html 缺少 meta description | 🟡 中等 | ✅ 已修复 |
| #9 | Font Awesome CDN 渲染阻塞 | 🟡 中等 | ✅ 已修复 |
| 页面 SEO 覆盖不全 | Category/Tag 等页面无标题描述 | 🟡 中等 | ✅ 已修复 |

### 未修复项（需要运维/架构层面配合）

| 审计编号 | 问题 | 原因 |
|----------|------|------|
| #3 | SPA 空白 HTML（根本问题） | 需评估 SSG/SSR 迁移，改动量大 |
| #7 | 404 返回 HTTP 200（软 404） | Nginx 配置不在仓库中，无法修改 |
| #10 | 内部链接不足 | 需内容积累，非代码层面 |
| #11 | 未注册搜索引擎平台 | 需运维账号操作 |

---

## 四、验证方法

```bash
# 1. 验证后端 slugs 端点
curl http://localhost:9090/user/articles/slugs

# 2. 验证后端 robots.txt
curl http://localhost:9090/robots.txt

# 3. 验证前端构建包含 robots.txt
ls frontend/dist/robots.txt

# 4. 验证文章页面 SEO（浏览器 DevTools → Elements）
# 检查 <head> 中是否有：
#   - <meta name="description">
#   - <meta property="og:title">
#   - <script type="application/ld+json">

# 5. Google 富结果测试
# 打开 https://search.google.com/test/rich-results
# 输入文章 URL

# 6. Lighthouse SEO 审计
npx lighthouse https://blog.hazenix.top --only-categories=seo
```
