# 设计：手记并入文章模型 + 总列表去封面

日期：2026-07-01
状态：待实现（brainstorming 已定稿）

## 背景与目标

两个诉求：

1. **总文章列表页去封面**：`ArticleList.vue` 列表项不再展示封面图。
2. **手记（手记/Moments）并入文章体系**：手记成为「手记」这一默认分类下的文章，从而自动参与个性化推荐系统。

现状约束（来自代码勘察）：

- 手记是一套独立且更简单的模型：`moment`（title/content/images JSON/like_count/view_count）+ `moment_tag`（字符串标签）+ `moment_like`（IP 幂等）。无分类、无 slug、无 userId、无 recommendLevel。
- 文章模型更丰富：`categoryId`、整型 `tag_id` 标签、slug、`recommendLevel`、coverImage 等。
- 推荐系统**完全以文章为中心**：`user_behavior.article_id`、`user_interest.tag_id`、`ContentBasedEngine` 基于整型 `tag_id` + `categoryId`，产物统一为 `ArticleShortVO`。手记当前完全在推荐管道之外。

关键决策（已与用户确认）：

- 线上手记**无需保留数据**，可直接废弃旧表、从零重建。
- `/moments` 页**保留朋友圈式图片九宫格 / MomentCard 展示**。
- 手记**不出现在总文章列表**，只在 `/moments` 页和推荐结果中出现。
- 后台**不合并**文章与手记管理台，但手记管理台**补齐全部文章管理能力**，并抽共享逻辑避免重复。
- 手记点赞**保留匿名 IP 幂等**（游客可点赞）。

## 方案选型

采用 **Approach A —— 将手记并入文章模型**：

- 手记 = 「手记」默认分类下的一条 `article` 记录。
- 因无历史数据，直接废弃 `moment` / `moment_tag` 表及并行的 Moment 后端，保留 `moment_like`（语义改为对 `article.id` 的 IP 幂等点赞）。
- 推荐系统**零改动**即可接入：手记就是带 `categoryId` + 整型 `tag_id` 的文章行。

已否决的替代方案：

- **Approach B（保留 moment 表 + 桥接进推荐）**：需教会每个引擎、`user_behavior`、`ArticleShortVO` 处理两种条目类型，长期维护双模型，工作量更大更乱，且现在没有数据需要保留，无收益。
- **Approach C（并入文章但图片塞进 markdown content）**：图片九宫格渲染要靠脆弱的 markdown 解析；一个可空列干净得多。

## Part 1：总列表去封面

**范围**：仅 `frontend/src/views/ArticleList.vue`（总文章列表页）。不动首页、分类页、推荐卡片等处的封面。

**改动**：

- 删除模板中封面块（现 33–42 行 `.article-cover` + `<img>`）。
- 删除 `getCoverUrl`、`imageErrors` 及相关引用（`getThumbnailUrl` 若无其它使用则一并清理 import）。
- 删除 `.article-cover` / `.cover-image` 样式。

**验收**：总列表项只剩标题 + 摘要 + 元信息 + 标签；无图片请求；深浅色正常。

## Part 2：手记并入文章

### 2.1 默认「手记」分类（环境无关识别）

- `category` 表新增 `type SMALLINT NOT NULL DEFAULT 0`（0=普通，1=手记）。
- 种子插入一条 `type=1` 的「手记」分类（迁移 SQL 中 `INSERT ... WHERE NOT EXISTS` 幂等）。
- 后端通过 `SELECT id FROM category WHERE type=1` 解析手记分类 id，**不硬编码 id**，跨环境安全。可加常量 `CategoryConstants.MOMENT_CATEGORY_TYPE = 1`。
- **禁止删除默认手记分类**：`CategoryService` 删除逻辑对 `type=1` 抛业务异常；前端 `CategoryManagement.vue` 对该分类隐藏/禁用删除按钮。

### 2.2 文章表加图片列

- `article` 新增 `images TEXT`（JSON 数组字符串，可空）。仅手记填充，复用 MomentEditor 现有 OSS 上传。
- 普通文章该列为 NULL，不受影响。

### 2.3 后端改造

- **废弃**：`moment` / `moment_tag` 表；`Moment` 实体、`MomentMapper`、`MomentTagMapper`、`MomentTag` 相关 POJO；`MomentServiceImpl` 中基于旧表的读写逻辑。
- **保留 API 形状**：`MomentController` 路由（`/moments` 等）与 `MomentVO` 字段形状保留，使前端 `/moments` + `MomentCard` 几乎无需改动。`MomentService` 重写为读写 `article` 表：
  - 查询：`categoryId = 手记分类 id AND status = 正常`，按创建时间倒序分页；填充 `images`、整型标签、`liked`（按 clientIp 查 `moment_like`）。
  - 写入（后台）：插入/更新 `article` 行，固定 `categoryId=手记`、`coverImage=null`、`slug` 可留空或按 id 兜底。
- **点赞保留匿名 IP 幂等**：保留 `moment_like` 表，迁移中**将列 `moment_id` 重命名为 `article_id`**（引用 `article.id`），Mapper 同步改名。手记点赞端点做 IP 幂等校验并 `article.like_count++`。文章原有登录态点赞（`user_article`）不用于手记。

### 2.4 后台手记管理（不合并、补齐能力）

- **列表**：保留独立「手记管理」入口，列展示贴合手记（正文摘要 + 图片缩略图 + 浏览/点赞 + 状态）。
- **补齐文章管理能力**：新增推荐度调节 (0–5)、置顶、状态发布/下架切换、查看、内容搜索（标题+正文）、批量删除已具备。
- **共享逻辑**：将行操作（改推荐度、切状态、置顶、删除、批量删除）抽为共享 composable（如 `useArticleAdminActions`），供 `ArticleManagement.vue` 与 `MomentManagement.vue` 共用，避免重复。
- **编辑器**：`MomentEditor` 写入手记分类文章行，字段为 标题(可选)、正文、图片九宫格、**文章整型标签**（替换旧字符串 tag 控件）；不显示封面/slug 输入。
- 后端管理接口：手记增删改走文章管理服务（带 categoryId=手记 约束），或复用 `ArticleService` 并在手记管理端点固定分类。

### 2.5 推荐系统（零改动接入 + 一处核对）

- 手记成为带 `categoryId` + 整型 `tag_id` 的文章行后，自动进入 `user_behavior` / `ContentBasedEngine` / CF / Popularity 管道。
- **实现时需核对**：推荐候选查询（`RecommendService` / 各引擎的候选文章 SQL）未按分类排除手记分类；手记详情页浏览/点赞会正常写入 `user_behavior`。

### 2.6 可见性与落地页

- **总列表 `ArticleList`**：查询排除手记分类（后端过滤 `category.type != 1`，或前端过滤兜底；优先后端）。
- **`/moments` 页**：UI 不变（MomentCard 九宫格），后端换为文章驱动。
- **推荐结果中的手记**：链接到文章详情页；`ArticleDetail` 在「手记 type」分类时渲染图片九宫格；无标题手记用正文摘要兜底标题。手记详情用 id 路由即可，无需 slug。

## 影响面与风险

- **DB 变更**：`category.type` 加列 + 手记分类种子；`article.images` 加列；`moment_like` 语义调整；**丢弃 `moment` / `moment_tag`**。需在 2C2G 服务器执行一次迁移 SQL。
- **后端**：`MomentService` 重写；`CategoryService` 删除守卫；手记管理端点接入文章服务。
- **前端**：`ArticleList` 去封面 + 排除手记；`MomentManagement` 补齐能力 + 抽共享 composable；`MomentEditor` 换标签控件；`ArticleDetail` 手记分类渲染九宫格；`CategoryManagement` 禁删手记分类。
- **风险点**：
  - 推荐候选 SQL 若隐式包含所有正常文章，手记会自动进入——需确认这是期望（是）。
  - `moment_like` 列语义变更需与迁移脚本、Mapper 同步，避免残留旧引用。
  - 手记无标题时，列表/详情/推荐卡片的标题兜底需统一（正文首行摘要）。

## 迁移 SQL 概要（实现时细化）

```sql
-- 1. 分类加类型列 + 种子手记分类
ALTER TABLE category ADD COLUMN type SMALLINT NOT NULL DEFAULT 0;
INSERT INTO category (name, type, ...)
SELECT '手记', 1, ...
WHERE NOT EXISTS (SELECT 1 FROM category WHERE type = 1);

-- 2. 文章加图片列
ALTER TABLE article ADD COLUMN images TEXT;

-- 3. moment_like 列重命名，语义改为引用 article.id
ALTER TABLE moment_like RENAME COLUMN moment_id TO article_id;

-- 4. 废弃旧表（确认无数据后）
DROP TABLE IF EXISTS moment_tag;
DROP TABLE IF EXISTS moment;
```

## 验收标准

- 总文章列表页无封面、无图片请求，手记不出现在总列表。
- `/moments` 页保持图片九宫格展示，游客可匿名点赞（IP 幂等）。
- 后台手记管理具备推荐度/置顶/状态切换/查看/内容搜索/批量删除，编辑器支持图片九宫格 + 整型标签。
- 新发布的手记能在推荐结果中出现，点击进入文章详情（九宫格渲染）。
- 手记详情页浏览/点赞写入 `user_behavior`，影响后续推荐。
- 手记默认分类不可删除。
