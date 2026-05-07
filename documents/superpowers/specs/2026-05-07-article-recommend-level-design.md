# 文章推荐度功能 — 设计文档

**日期**: 2026-05-07
**状态**: 设计确认
**作者**: HazeHacker
**依赖**: `2026-04-30-recommendation-system`（个性化推荐系统 v1）

---

## 1. 背景与目标

现有推荐系统（v1）通过三路引擎（内容 40% + 协同过滤 40% + 热度 20%）自动计算每篇文章的推荐分。但管理员缺少对推荐结果的干预手段：

- 无法主推一篇冷门但优质的文章
- 无法把某篇文章从推荐位拿下
- 无法标注"精华"让用户在推荐区感知到

本次改良引入**推荐度（recommend_level）**字段，让 admin 在发布/编辑文章时指定 0-5 的推荐等级，作为全局乘数影响推荐排序，并在前端推荐卡片上以徽章形式对高推荐度文章做视觉标识。

### 关键设计决策（已与用户确认）

| 决策点 | 选择 |
|-------|------|
| 推荐度取值 | 0-5 整数，默认 3 |
| 生效位置 | 作为全局乘数，应用于所有推荐路径（匿名 / 冷启动 / 活跃用户）最终分数 |
| 推荐度 0 的语义 | 硬屏蔽，从候选池直接排除 |
| 缓存更新策略 | 修改后立即清除所有推荐缓存 |
| 前端视觉展示 | 仅在推荐卡片上，4 档"推荐"徽章、5 档"精华"徽章 |
| admin 入口 | 发布/编辑表单 + admin 文章列表页快捷编辑 |

---

## 2. 推荐度语义

```
0 · 不推荐（屏蔽）     — 完全不出现在任何推荐场景
1 · 弱                — 基础分 ×0.70
2 · 较弱              — 基础分 ×0.85
3 · 默认              — 基础分 ×1.00（等同于未设置）
4 · 推荐              — 基础分 ×1.15，前端显示"推荐"徽章
5 · 精华              — 基础分 ×1.30，前端显示"精华"徽章
```

**乘数公式**
```
multiplier(level) = 1.0 + (level - 3) × 0.15   // 适用于 level ∈ {1, 2, 3, 4, 5}
level = 0 => 从候选池排除
level = null（旧数据）=> 等同于 3（乘数 1.0）
```

**不影响的场景**
- 文章详情页、分类页、标签页、搜索结果、文章列表等「非推荐」位置，均不受推荐度影响（推荐度只是推荐系统的排序信号）

---

## 3. 数据模型

### 3.1 Article 表变更

```sql
ALTER TABLE article
ADD COLUMN recommend_level TINYINT NOT NULL DEFAULT 3
COMMENT '推荐度 0=屏蔽 1-5=推荐等级(3为默认)';

CREATE INDEX idx_recommend_level ON article(recommend_level);
```

索引用途：`recomputeContentSimilarityMatrix` 等遍历 article 表时需要按 `recommend_level != 0` 过滤。

### 3.2 Article 实体新增字段

```java
@ApiModelProperty(value = "推荐度[0:不推荐 | 1-5:推荐等级，默认3]", example = "3")
private Integer recommendLevel;
```

### 3.3 ArticleDTO 新增字段

```java
@ApiModelProperty(value = "推荐度[0-5，默认3]", example = "3")
@Min(value = 0, message = "推荐度最小值为0")
@Max(value = 5, message = "推荐度最大值为5")
private Integer recommendLevel;
```

### 3.4 ArticleShortVO 新增字段

```java
@ApiModelProperty(value = "推荐度", example = "3")
private Integer recommendLevel;
```

前端推荐卡片需要读取此字段判断是否显示徽章。

---

## 4. 常量定义

`RecommendConstants` 新增：

```java
// 推荐度 (article.recommend_level)
public static final int RECOMMEND_LEVEL_BLOCKED = 0;
public static final int RECOMMEND_LEVEL_DEFAULT = 3;
public static final int RECOMMEND_LEVEL_MAX = 5;
public static final double RECOMMEND_LEVEL_STEP = 0.15;
```

---

## 5. 推荐算法改动

### 5.1 乘数工具方法

在 `RecommendServiceImpl` 中新增私有方法：

```java
private double recommendMultiplier(Article article) {
    Integer level = article.getRecommendLevel();
    if (level == null) return 1.0;
    return 1.0 + (level - RecommendConstants.RECOMMEND_LEVEL_DEFAULT)
                 * RecommendConstants.RECOMMEND_LEVEL_STEP;
}

private boolean isBlocked(Article article) {
    Integer level = article.getRecommendLevel();
    return level != null && level == RecommendConstants.RECOMMEND_LEVEL_BLOCKED;
}
```

### 5.2 三个计算方法的修改

**`computeFullRecommendations`（登录活跃用户）**

```java
for (Article article : allArticles) {
    if (isBlocked(article)) continue;                        // 新增：硬屏蔽
    if (viewedArticleIds.contains(article.getId())) continue;

    // ... 原有 content/cf/pop 计算逻辑 ...

    double baseScore = contentScore * WEIGHT_CONTENT
                     + cfScore * WEIGHT_CF
                     + popScore * WEIGHT_POPULARITY;
    finalScores.put(article.getId(), baseScore * recommendMultiplier(article));  // 修改
}
```

**`computeColdStartRecommendations`（冷启动用户）**

```java
for (Article article : allArticles) {
    if (isBlocked(article)) continue;                        // 新增

    // ... 原有 content + pop 计算 ...

    double baseScore = contentScore * COLD_WEIGHT_CONTENT
                     + popScore * COLD_WEIGHT_POPULARITY;
    finalScores.put(article.getId(), baseScore * recommendMultiplier(article));  // 修改
}
```

**`recomputeAnonymousRecommendations`（匿名用户）**

```java
for (Article article : allArticles) {
    if (isBlocked(article)) continue;                        // 新增
    if (excludeIds.contains(article.getId())) continue;

    // ... 原有 pop + diversity 计算 ...

    double baseScore = popScore * ANON_WEIGHT_POPULARITY
                     + popScore * ANON_WEIGHT_FRESHNESS
                     + diversityScore * ANON_WEIGHT_DIVERSITY;
    scores.put(article.getId(), baseScore * recommendMultiplier(article));       // 修改
}
```

### 5.3 侧边栏热门文章过滤

`PopularArticleServiceImpl.recompute()` 中排除 `recommend_level = 0` 的文章：

```java
// SQL 层面（ArticleMapper.listAllForScoring）或内存层面加过滤
allArticles.stream()
    .filter(a -> a.getRecommendLevel() == null || a.getRecommendLevel() != 0)
    .forEach(...);
```

推荐在 `ArticleMapper.listAllForScoring` 的 SQL 中加 `WHERE recommend_level != 0 OR recommend_level IS NULL`，从源头过滤。

### 5.4 内容相似度矩阵

`recomputeContentSimilarityMatrix` 中，0 档文章不需要计算相似度（既不会被推荐出去，也不需要被作为相似度参考）：

```java
List<Article> allArticles = articleMapper.listAllForScoring();
// allArticles 已由 SQL 过滤了 level = 0
```

---

## 6. API 设计

### 6.1 新增：更新推荐度

```
PUT /admin/articles/{id}/recommend-level?level={0-5}
Authorization: Bearer <admin-token>

Response:
{
  "code": 200,
  "msg": "success"
}
```

**控制器**
```java
@PutMapping("/{id}/recommend-level")
public Result updateRecommendLevel(
    @PathVariable Long id,
    @RequestParam @Min(0) @Max(5) Integer level) {
    log.info("管理员修改文章 {} 推荐度为 {}", id, level);
    articleService.updateRecommendLevel(id, level);
    return Result.success();
}
```

**Service 实现**
```java
@Override
@Transactional
public void updateRecommendLevel(Long id, Integer level) {
    articleMapper.updateRecommendLevel(id, level);
    recommendCacheService.evictAllRecommendations();
}
```

**Mapper 方法**
```java
void updateRecommendLevel(@Param("id") Long id, @Param("level") Integer level);
```

XML:
```xml
<update id="updateRecommendLevel">
    UPDATE article SET recommend_level = #{level} WHERE id = #{id}
</update>
```

### 6.2 修改：发布 / 编辑文章

`POST /admin/articles` 和 `PUT /admin/articles/{id}` 接口的 `ArticleDTO` 透传 `recommendLevel`，Service 层写入数据库。若编辑时 `recommendLevel` 发生变化，同步调用 `evictAllRecommendations()`。

### 6.3 修改：查询文章列表

admin 文章列表接口的返回值需要包含 `recommend_level` 字段，供前端快捷编辑 UI 使用。

---

## 7. 缓存清除

### 7.1 新增方法 `RecommendCacheService.evictAllRecommendations`

```java
public void evictAllRecommendations() {
    try {
        redisTemplate.delete(KEY_REC_ANONYMOUS);

        // 注：用户量 > 1w 时应改用 SCAN 避免阻塞
        Set<String> userKeys = redisTemplate.keys(KEY_REC_USER + "*");
        Set<String> coldKeys = redisTemplate.keys(KEY_REC_COLD + "*");

        if (userKeys != null && !userKeys.isEmpty()) redisTemplate.delete(userKeys);
        if (coldKeys != null && !coldKeys.isEmpty()) redisTemplate.delete(coldKeys);

        log.info("已清除所有推荐缓存: user={}, cold={}",
            userKeys == null ? 0 : userKeys.size(),
            coldKeys == null ? 0 : coldKeys.size());
    } catch (Exception e) {
        log.error("清除推荐缓存失败", e);
    }
}
```

### 7.2 触发时机

| 操作 | 是否触发清缓存 |
|------|---------------|
| 调用 `updateRecommendLevel` 接口 | ✓ 触发 |
| 发布文章（`recommendLevel != 3`）| ✓ 触发 |
| 编辑文章时 `recommendLevel` 发生变化 | ✓ 触发 |
| 编辑文章但 `recommendLevel` 未变 | ✗ 不触发 |
| 删除文章 | （既有逻辑，不在本次范围）|

---

## 8. 前端改动

### 8.1 admin 发布 / 编辑表单

在文章发布/编辑页面，在 `isTop` 字段附近加入：

```vue
<el-form-item label="推荐度">
  <el-select v-model="form.recommendLevel" style="width: 200px">
    <el-option :value="0" label="0 · 不推荐（屏蔽）" />
    <el-option :value="1" label="1 · 弱" />
    <el-option :value="2" label="2 · 较弱" />
    <el-option :value="3" label="3 · 默认" />
    <el-option :value="4" label="4 · 推荐" />
    <el-option :value="5" label="5 · 精华" />
  </el-select>
  <span class="text-xs text-gray-400 ml-2">
    影响文章在推荐位的曝光权重
  </span>
</el-form-item>
```

初始化时 `form.recommendLevel = 3`（新建文章）或 `article.recommendLevel`（编辑）。

### 8.2 admin 文章列表页快捷编辑

在文章列表表格新增一列：

```vue
<el-table-column label="推荐度" width="140" align="center">
  <template #default="{ row }">
    <el-dropdown trigger="click" @command="(lv) => changeLevel(row, lv)">
      <el-tag
        :type="levelTagType(row.recommendLevel)"
        class="cursor-pointer"
      >
        {{ levelLabel(row.recommendLevel) }}
      </el-tag>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="lv in [0,1,2,3,4,5]"
            :key="lv"
            :command="lv"
            :disabled="lv === row.recommendLevel"
          >
            {{ levelLabel(lv) }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </template>
</el-table-column>
```

```js
const levelLabel = (lv) => {
  const map = { 0: '屏蔽', 1: '弱', 2: '较弱', 3: '默认', 4: '推荐', 5: '精华' }
  return map[lv ?? 3]
}

const levelTagType = (lv) => {
  if (lv === 0) return 'info'
  if (lv === 5) return 'danger'
  if (lv === 4) return 'warning'
  return ''
}

const changeLevel = async (row, lv) => {
  await updateRecommendLevel(row.id, lv)
  row.recommendLevel = lv
  ElMessage.success('推荐度已更新')
}
```

### 8.3 API 调用

`frontend/src/api/article.js` 新增：

```js
export function updateRecommendLevel(id, level) {
  return request({
    url: `/admin/articles/${id}/recommend-level`,
    method: 'put',
    params: { level }
  })
}
```

### 8.4 RecommendCard 徽章

修改 `frontend/src/components/article/RecommendCard.vue`：

```vue
<template>
  <div class="relative flex-shrink-0 w-72 ...">  <!-- 添加 relative -->
    <!-- 推荐度徽章 -->
    <div v-if="article.recommendLevel === 5"
         class="absolute top-2 right-2 px-2 py-0.5 bg-primary text-white text-xs rounded shadow z-10">
      精华
    </div>
    <div v-else-if="article.recommendLevel === 4"
         class="absolute top-2 right-2 px-2 py-0.5 bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 text-xs rounded shadow z-10">
      推荐
    </div>

    <!-- 原有卡片内容 -->
    ...
  </div>
</template>
```

---

## 9. 测试计划

### 9.1 单元测试

**`RecommendServiceImplTest`（新增）**

| 用例 | 预期 |
|------|------|
| `recommendMultiplier_level_3_returns_1` | level=3 返回 1.0 |
| `recommendMultiplier_level_5_returns_1_30` | level=5 返回 1.30 |
| `recommendMultiplier_level_1_returns_0_70` | level=1 返回 0.70 |
| `recommendMultiplier_null_returns_1` | level=null 返回 1.0 |
| `isBlocked_level_0_returns_true` | level=0 被识别为 blocked |
| `updateRecommendLevel_evicts_all_caches` | 更新后 evictAllRecommendations 被调用 |

### 9.2 端到端验证

| 场景 | 预期结果 |
|------|---------|
| 发布推荐度 5 文章 | 首页推荐区出现"精华"徽章，排序明显靠前 |
| 已发布文章改推荐度为 0 | 首页推荐区 / 侧边栏热门文章立即消失；缓存已清 |
| 数据相近的 level=1 vs level=5 两文对比 | level=5 排序明显靠前 |
| admin 列表页点击 tag 切换 | 下拉展开，选中后立即生效，tag 文案更新 |
| 切回默认 3 | 徽章消失，排序恢复到只看三路引擎的结果 |

### 9.3 影响范围

| 影响方 | 变化 |
|------|------|
| 匿名用户推荐条 | 0 档消失，1-5 档按乘数重排 |
| 侧边栏热门文章 | 0 档消失 |
| 冷启动用户推荐 | 0 档消失，1-5 档按乘数重排 |
| 活跃用户个性化推荐 | 0 档消失，1-5 档按乘数重排 |
| 推荐卡片 | 4/5 档显示徽章 |
| 文章详情页 / 分类页 / 搜索 | 不受影响 |

---

## 10. 兼容性与迁移

- 迁移脚本中 `DEFAULT 3` 确保所有存量文章默认为中性推荐度，推荐排序结果不变
- 旧数据 `recommendLevel = null` 的情况（理论上迁移后不会出现，但代码仍需兼容）等同于 3 档
- 无需向前兼容旧 API 版本，因为新增字段是可选的，旧客户端不传即使用默认

---

## 11. YAGNI 说明

以下功能**不在本次范围**内：

- 推荐度历史记录 / 审计日志（admin 改动频率低，Git + log 已足够）
- 推荐度定时重算（admin 主动标记的值不应被系统覆盖）
- 按推荐度筛选文章列表（非核心需求，admin 需要时可借助搜索）
- 批量修改推荐度（一次改一个够用）
- 推荐度的 A/B 测试（博客规模不需要）
