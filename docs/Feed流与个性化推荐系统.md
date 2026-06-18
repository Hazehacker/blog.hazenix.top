# Feed 流与个性化推荐系统

> 本文档覆盖两部分：**Feed 流通用知识**（面试理论储备）+ **本项目推荐系统的完整实现**（亮点/难点梳理）。

---

## 一、Feed 流基础知识

### 1.1 什么是 Feed 流

Feed 流（Information Feed / News Feed）是一种内容分发模式，把关注、兴趣或系统判断用户可能喜欢的内容按时间或算法排序，持续推送到用户的"信息流"中。典型场景：微博、朋友圈、微信公众号、抖音推荐页、知乎首页。

Feed 流分两大类：

| 类型 | 描述 | 代表产品 |
|------|------|---------|
| Timeline Feed | 纯按时间排序，显示关注对象的新内容 | 早期 Twitter、微博 |
| Ranked Feed | 算法排序，按用户偏好/互动预测重排 | 抖音、微信视频号、现在的 Twitter/X |

### 1.2 三种推送架构（面试高频）

#### 推模式（Push / Fan-out on Write）

用户发布内容后，系统**立即**遍历他的所有粉丝，把内容写入每个粉丝的 Feed 列表（Redis List / 独立收件箱）。

```
发布者 → [写放大] → 粉丝1的 inbox、粉丝2的 inbox、...
```

- 优点：读取 Feed 时极快，O(1) 直接读用户收件箱
- 缺点：大 V 有百万粉丝时，一次发布会触发百万次写操作（写放大严重）
- 适合：粉丝数量适中、读多写少的场景

#### 拉模式（Pull / Fan-out on Read）

用户打开 Feed 时，系统**实时**查询他关注的所有人的最新内容，在读取时聚合排序。

```
读取时 → 查关注列表 → 遍历每人的发布记录 → 聚合 → 排序返回
```

- 优点：无写放大，数据存储简单
- 缺点：读时聚合慢，关注多时延迟高（N 次查询）
- 适合：大 V 侧（关注者少但粉丝多）

#### 推拉结合（混合模式）

业界主流（如微博）：

- **普通用户**（粉丝 < 某阈值）：推模式，写时 fan-out
- **大 V 用户**（粉丝量超阈值）：拉模式，读时合并
- 用户打开 Feed：拉取自己收件箱（推模式结果）+ 拉取关注大 V 的最新内容，合并排序

### 1.3 RSS Feed（订阅协议）

RSS（Really Simple Syndication）是一种基于 XML 的内容聚合协议，让用户通过 RSS 阅读器订阅博客/新闻而无需反复访问网站。格式如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <title>博客标题</title>
    <link>https://example.com</link>
    <item>
      <title>文章标题</title>
      <link>https://example.com/article/1</link>
      <pubDate>Mon, 01 Jan 2024 00:00:00 +0800</pubDate>
      <description><![CDATA[摘要]]></description>
    </item>
  </channel>
</rss>
```

### 1.4 推荐系统三大路径

业界个性化推荐通常分三层：

```
召回（Recall） → 粗排（Pre-Ranking） → 精排（Ranking）
```

| 阶段 | 目标 | 方法举例 |
|------|------|---------|
| 召回 | 从全库百万文章中快速筛出千级候选集 | 协同过滤、内容相似度、热度榜 |
| 粗排 | 从千级候选排出百级 | 简单模型打分 |
| 精排 | 最终排序 | 深度学习（双塔模型、DIN 等） |

本项目规模较小（文章数十至数百篇），直接对全库做精排，无需召回分层。

---

## 二、本项目 Feed 流的两个实现

本项目包含两个独立的"Feed"功能：

1. **RSS Feed**：`GET /feed` — 标准 XML 订阅协议，供 RSS 阅读器使用
2. **个性化推荐 Feed**：`GET /user/articles/recommended` — 首页"为你推荐"横向滚动卡片

---

## 三、RSS Feed 实现

### 3.1 接口

```
GET /feed
Content-Type: application/rss+xml; charset=UTF-8
```

### 3.2 实现要点

文件：`FeedController.java`

- 查询最新 20 篇已发布文章（`status = 0`）
- 手动拼接 XML 字符串（不依赖第三方库，轻量）
- pubDate 用 RFC 822 格式（RSS 规范要求）：`EEE, dd MMM yyyy HH:mm:ss Z`
- 文章 URL 优先用 slug（对 SEO 友好），无 slug 时退化为 ID
- 正文截取前 300 字符作为摘要，用 `CDATA` 包裹避免 XML 转义问题

### 3.3 设计决策

| 点 | 决策 |
|----|------|
| XML 构建方式 | 手动拼接（规模小，无需引入 Rome/JAXB 等依赖） |
| 文章数量 | 固定 20 条，足够 RSS 场景 |
| 摘要来源 | content 字段去 HTML 标签后截取，而非 summary 字段 |

---

## 四、个性化推荐系统详解

### 4.1 整体架构

```
用户请求 /user/articles/recommended
         │
         ▼
 RecommendServiceImpl.getRecommendations(userId, size)
         │
    ┌────┴────┐
    │         │
 userId=null  userId有值
    │         │
    ▼         ▼
 匿名推荐   ┌─ 读 Redis 缓存（命中则返回）
            │
            └─ 冷启动判断
                ├─ 行为数 ≥ 5  → computeFullRecommendations（三路混合）
                ├─ 有兴趣标签  → computeColdStartRecommendations（内容+热度）
                └─ 否则        → 退化为匿名推荐（热度）
```

### 4.2 三种用户状态与对应策略

#### 匿名用户（未登录）

```
推荐分 = 热度分 × 0.5 + 新鲜度分 × 0.3 + 多样性分 × 0.2
```

- 热度分 = 浏览数 + 点赞数×10 + 收藏数×20，再乘时间衰减 `e^(-0.01 × 天数)`
- 多样性分 = `min(1.0, 标签数 / 5)` — 鼓励标签丰富的文章
- 结果缓存在 Redis，全局共享，TTL 1 小时
- 首页"换一批"：因全局共享缓存，匿名用户前端直接洗牌，不发请求

#### 冷启动用户（登录但行为 < 5 条，有兴趣标签）

注册时引导用户选择感兴趣的标签（存入 `user_interest` 表），此阶段使用：

```
推荐分 = 内容相关度 × 0.6 + 热度分 × 0.4
```

内容相关度 = `Σ(用户兴趣标签权重) / 文章标签总数`（与文章标签匹配越多越高）

#### 活跃用户（行为 ≥ 5 条）

三路混合引擎：

```
推荐分 = 内容分 × 0.4 + 协同过滤分 × 0.4 + 热度分 × 0.2
```

同时排除用户已读过的文章。

### 4.3 三个引擎详解

#### ContentBasedEngine（内容推荐）

**文章间相似度**：Jaccard 系数 + 同分类加分

```java
Jaccard(A, B) = |tags_A ∩ tags_B| / |tags_A ∪ tags_B|
// 同分类额外 +0.2（上限 1.0）
```

**用户→文章得分**：

```java
score = Σ(interest.weight for tag in article_tags) / article_tags.size
```

#### CollaborativeFilterEngine（协同过滤）

基于**物品的协同过滤（Item-CF）**：

1. 构建用户-文章评分矩阵（行为→隐式评分）
2. 计算文章间余弦相似度：
   ```
   sim(A, B) = (共同用户的评分向量点积) / (|向量A| × |向量B|)
   ```
3. 对候选文章打分：
   ```
   cfScore(candidate) = Σ(userRating[i] × sim(candidate, i)) / Σ|sim(candidate, i)|
   ```

**隐式评分规则**：

| 行为 | 分值 |
|------|------|
| 浏览（< 30s） | 1 |
| 浏览（≥ 30s） | 2 |
| 点赞 | 3 |
| 收藏 | 5 |

#### PopularityEngine（热度引擎）

```java
rawScore = viewCount + likeCount × 10 + favoriteCount × 20
popularityScore = rawScore × e^(-0.01 × 发布天数)
```

时间衰减使用指数衰减（λ=0.01），约 69 天后热度减半。

### 4.4 推荐度（recommend_level）— 管理员干预机制

为了让管理员能够干预推荐结果，文章表新增 `recommend_level` 字段（0-5，默认 3）：

| 等级 | 语义 | 乘数 |
|------|------|------|
| 0 | 屏蔽（不出现在任何推荐位） | 直接排除 |
| 1 | 弱推荐 | ×0.70 |
| 2 | 较弱 | ×0.85 |
| 3 | 默认 | ×1.00 |
| 4 | 推荐（前端显示"推荐"徽章） | ×1.15 |
| 5 | 精华（前端显示"精华"徽章） | ×1.30 |

乘数公式：`multiplier = 1.0 + (level - 3) × 0.15`

**这个设计的价值**：纯算法推荐无法感知内容质量，管理员可以主推冷门好文（level=5）或下架写得差的文章（level=0），人工+算法结合。

### 4.5 Redis 缓存设计

所有计算结果缓存在 Redis ZSet（有序集合），score 字段存推荐分，天然支持按分值 `reverseRange` 取 TopN。

| Redis Key | 内容 | TTL |
|-----------|------|-----|
| `rec:anonymous` | 匿名推荐文章列表 | 1h |
| `rec:user:{userId}` | 活跃用户推荐列表 | 2h |
| `rec:cold:{userId}` | 冷启动用户推荐列表 | 2h |
| `sim:content:{articleId}` | 文章内容相似度矩阵 | 24h |
| `sim:cf:{articleId}` | 文章 CF 相似度矩阵 | 12h |

**为什么用 ZSet？** ZSet 的 member 是文章 ID，score 是推荐分，`ZREVRANGE key 0 9` 直接返回 Top10，无需在内存中二次排序。

### 4.6 定时任务（预计算）

```
凌晨 2:00 → recomputeCFMatrix（协同过滤矩阵，12h TTL）
凌晨 3:00 → recomputeContentMatrix（内容相似度矩阵，24h TTL）
每整点     → refreshAnonymousRecommendations（匿名推荐，1h TTL）
应用启动   → 预热匿名推荐 + 内容相似度矩阵
```

协同过滤矩阵计算量最大（O(n²) 文章对），因此选在流量最低的凌晨运行。

### 4.7 行为数据收集

文件：`UserBehaviorController.java` / `UserBehaviorServiceImpl.java`

- 浏览行为：文章页离开时上报阅读时长（`POST /user/behaviors/view`）
- 点赞/收藏：在对应操作时同步触发 `@Async` 异步写入
- 行为数据存入 MySQL `user_behavior` 表，不实时影响推荐（下次重算时生效）

### 4.8 冷启动解决方案

冷启动是推荐系统的经典难题：新用户没有历史行为，无法个性化推荐。本项目的解法：

1. **注册时选兴趣标签**（`InterestTagSelector.vue`）：用户注册后弹出兴趣标签选择器，选择结果写入 `user_interest` 表，来源标记为 `INTEREST_SOURCE_REGISTER`
2. **冷启动策略**（行为 < 5 条但有兴趣标签）：只用内容推荐（60%）+ 热度（40%），不走 CF（CF 需要足够的用户间交叉行为才有效）
3. **兜底**：既无行为也无兴趣标签，降级为匿名热度推荐

### 4.9 前端实现

**RecommendSection.vue**（首页推荐区）：
- 横向可滚动卡片带（`overflow-x: auto`），左右箭头按钮（hover 显示）
- 登录用户：点击"换一批"调用 `POST /user/articles/recommended/refresh`（清缓存重计算）
- 匿名用户：点击"换一批"仅做前端数组 shuffle，不发请求（全局缓存重请求结果相同）

**RecommendCard.vue**：
- 推荐度 4 级：显示"推荐"灰色徽章
- 推荐度 5 级：显示"精华"主色徽章

---

## 五、设计亮点与难点总结

### 亮点

1. **三路混合引擎**：单一推荐策略容易偏颇（CF 有冷启动问题，内容推荐过于保守，热度会马太效应）。三路混合各取所长，权重可调。

2. **推荐度人工干预**：纯算法推荐的"盲点"是内容质量感知不足。管理员可设置 0-5 级推荐度，作为全局乘数叠加在算法分上，相当于在算法之外加了一个编辑策略层。

3. **Redis ZSet 的精妙应用**：用 ZSet 的 score 存推荐分，天然有序，读取 Top-N 是 O(log N + K)，比把结果序列化成 List 存 String 更高效，且支持动态更新单条 score。

4. **冷启动三阶段**：无行为→降级匿名；有兴趣标签→冷启动策略；行为足够→完整三路混合，覆盖了用户成长的全生命周期。

5. **时间衰减**：热度分乘以 `e^(-λt)`，旧文章不会因为历史积累的高浏览量永远占据推荐位。

### 难点

1. **协同过滤矩阵是 O(n²)**：文章数 n 篇则需计算 n×n 的相似度矩阵，文章多时耗时显著。本项目设置了 CF 最低行为阈值（5 次）过滤冷门文章，缩小矩阵规模；矩阵重算放在凌晨低峰期。

2. **缓存一致性**：推荐度变更时需要立即失效所有用户缓存。当前用 `KEYS` 命令批量删除，在用户量小时没问题；如果用户量增大到万级，需改用 `SCAN` 避免阻塞 Redis。

3. **行为数据的隐式反馈**：用户没有显式评分，只有行为日志。把浏览/点赞/收藏转化为 1/3/5 的隐式评分是工程上的取舍，不同权重设置对推荐质量影响较大，需要结合实际数据调优。

---

## 六、数据库表设计

```sql
-- 用户行为记录
CREATE TABLE user_behavior (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT   NOT NULL,
    article_id    BIGINT   NOT NULL,
    behavior_type TINYINT  NOT NULL  COMMENT '1=浏览 2=点赞 3=收藏',
    duration      INT      DEFAULT NULL COMMENT '阅读时长(秒)',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_behavior (user_id, behavior_type),
    INDEX idx_article_behavior (article_id, behavior_type)
);

-- 用户兴趣标签（注册选择 + 行为推断）
CREATE TABLE user_interest (
    id          BIGINT  AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT  NOT NULL,
    tag_id      BIGINT  NOT NULL,
    weight      DOUBLE  NOT NULL DEFAULT 0.5 COMMENT '兴趣权重 0-1',
    source      TINYINT NOT NULL COMMENT '1=注册选择 2=行为推断',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_tag (user_id, tag_id)
);

-- 文章表新增字段
ALTER TABLE article
ADD COLUMN recommend_level TINYINT NOT NULL DEFAULT 3
COMMENT '推荐度 0=屏蔽 1-5=推荐等级(3为默认)';
```

---

## 七、接口清单

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/feed` | RSS Feed（XML） | 无需 |
| GET | `/user/articles/recommended?size=10` | 获取推荐文章 | 可选（有无登录效果不同） |
| POST | `/user/articles/recommended/refresh?size=10` | 换一批（清缓存重算） | 需要登录 |
| POST | `/user/behaviors/view` | 上报浏览行为+时长 | 需要登录 |
| PUT | `/admin/articles/{id}/recommend-level?level=4` | 管理员设置推荐度 | 需要 admin |
| GET/PUT | `/user/interests` | 查询/设置兴趣标签 | 需要登录 |
