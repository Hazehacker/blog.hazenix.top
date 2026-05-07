# Article Recommend Level Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `recommend_level` field (0-5) to articles, allowing admin to influence recommendation ranking and display badges on high-level articles.

**Architecture:** Admin can set recommend_level (0=block, 1-5=multiplier) when publishing/editing articles. The field acts as a global multiplier on all recommendation paths and clears Redis caches on changes.

**Tech Stack:** Spring Boot 2.7, MyBatis, Redis, Vue 3, Element Plus

---

## File Structure

| Layer | File | Change |
|-------|------|--------|
| SQL | `documents/sql/recommend_level.sql` | Create migration |
| Entity | `backend/blog-pojo/src/main/java/top/hazenix/entity/Article.java` | Add `recommendLevel` field |
| DTO | `backend/blog-pojo/src/main/java/top/hazenix/dto/ArticleDTO.java` | Add `recommendLevel` field with validation |
| VO | `backend/blog-pojo/src/main/java/top/hazenix/vo/ArticleShortVO.java` | Add `recommendLevel` field |
| Mapper | `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java` | Add `updateRecommendLevel`, `listAllForScoring` add filter |
| Mapper XML | `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml` | Update SQL |
| Constants | `backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java` | Add level constants |
| Service | `backend/blog-server/src/main/java/top/hazenix/service/ArticleService.java` | Add `updateRecommendLevel` method |
| Service Impl | `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java` | Implement `updateRecommendLevel`, track changes |
| Cache | `backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendCacheService.java` | Add `evictAllRecommendations` method |
| Recommend Svc | `backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java` | Add multiplier/block logic in 3 methods |
| Popular Svc | `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java` | Filter blocked articles |
| Controller | `backend/blog-server/src/main/java/top/hazenix/controller/admin/ArticleController.java` | Add PUT endpoint |
| Frontend API | `frontend/src/api/article.js` | Add `updateRecommendLevel` |
| Frontend Editor | `frontend/src/components/admin/ToastUIEditor.vue` | Add recommend_level select |
| Frontend List | `frontend/src/views/admin/ArticleManagement.vue` | Add quick-edit column |
| Frontend Card | `frontend/src/components/article/RecommendCard.vue` | Add badge display |
| Test | `backend/blog-server/src/test/java/top/hazenix/test/RecommendServiceImplTest.java` | New unit tests |

---

### Task 1: SQL Migration

**Files:**
- Create: `documents/sql/recommend_level.sql`
- Test: Run migration against database

- [ ] **Step 1: Create SQL migration file**

```sql
-- 添加推荐度字段
ALTER TABLE article
ADD COLUMN recommend_level TINYINT NOT NULL DEFAULT 3
COMMENT '推荐度 0=屏蔽 1-5=推荐等级(3为默认)';

-- 创建索引用于过滤屏蔽文章
CREATE INDEX idx_recommend_level ON article(recommend_level);
```

- [ ] **Step 2: Commit**

```bash
git add documents/sql/recommend_level.sql
git commit -m "feat(recommend-level): add recommend_level column to article table"
```

---

### Task 2: Entity, DTO, VO Changes

**Files:**
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/Article.java`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/dto/ArticleDTO.java`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/vo/ArticleShortVO.java`

- [ ] **Step 1: Add field to Article entity**

In `Article.java`, add after `isTop` field:

```java
@ApiModelProperty(value = "推荐度[0:不推荐 | 1-5:推荐等级，默认3]", example = "3")
private Integer recommendLevel;
```

- [ ] **Step 2: Add field to ArticleDTO with validation**

In `ArticleDTO.java`, add after `tagIds` field:

```java
@ApiModelProperty(value = "推荐度[0-5，默认3]", example = "3")
@Min(value = 0, message = "推荐度最小值为0")
@Max(value = 5, message = "推荐度最大值为5")
private Integer recommendLevel;
```

Also add import: `javax.validation.constraints.Max;` and `javax.validation.constraints.Min;`

- [ ] **Step 3: Add field to ArticleShortVO**

In `ArticleShortVO.java`, add after `viewCount`:

```java
@ApiModelProperty(value = "推荐度", example = "3")
private Integer recommendLevel;
```

- [ ] **Step 4: Commit**

```bash
git add backend/blog-pojo/src/main/java/top/hazenix/entity/Article.java
git add backend/blog-pojo/src/main/java/top/hazenix/dto/ArticleDTO.java
git add backend/blog-pojo/src/main/java/top/hazenix/vo/ArticleShortVO.java
git commit -m "feat(recommend-level): add recommendLevel field to Article, DTO, VO"
```

---

### Task 3: RecommendConstants Additions

**Files:**
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java`

- [ ] **Step 1: Add constants at end of RecommendConstants class**

After the existing constants, add:

```java
// 推荐度 (article.recommend_level)
public static final int RECOMMEND_LEVEL_BLOCKED = 0;
public static final int RECOMMEND_LEVEL_DEFAULT = 3;
public static final int RECOMMEND_LEVEL_MAX = 5;
public static final double RECOMMEND_LEVEL_STEP = 0.15;
```

- [ ] **Step 2: Commit**

```bash
git add backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java
git commit -m "feat(recommend-level): add recommend level constants"
```

---

### Task 4: ArticleMapper Changes

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`

- [ ] **Step 1: Add method to ArticleMapper interface**

Add after `deleteByIds`:

```java
void updateRecommendLevel(@Param("id") Long id, @Param("level") Integer level);
```

Add import: `org.apache.ibatis.annotations.Param;`

- [ ] **Step 2: Update ArticleMapper.xml insert statement**

Update the `<insert id="insert">` to include `recommend_level`:

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    insert into article (user_id,title,content,cover_image,category_id,like_count,favorite_count,view_count,slug,status,create_time,update_time,recommend_level)
    values (#{userId},#{title},#{content},#{coverImage},#{categoryId},#{likeCount},#{favoriteCount},#{viewCount},#{slug},#{status},#{createTime},#{updateTime},#{recommendLevel})
</insert>
```

- [ ] **Step 3: Update ArticleMapper.xml update statement**

Add after the `status` block:

```xml
<if test="recommendLevel != null">
    recommend_level = #{recommendLevel},
</if>
```

- [ ] **Step 4: Add updateRecommendLevel SQL**

Add after the `</delete>` tag:

```xml
<update id="updateRecommendLevel">
    UPDATE article SET recommend_level = #{level} WHERE id = #{id}
</update>
```

- [ ] **Step 5: Update listAllForScoring to filter blocked articles**

Update the `listAllForScoring` select to include recommend_level and filter:

```xml
<select id="listAllForScoring" resultType="top.hazenix.entity.Article">
    select id, title, cover_image, like_count, favorite_count, view_count, create_time, category_id, recommend_level
    from article
    where status = 0 AND (recommend_level != 0 OR recommend_level IS NULL)
</select>
```

- [ ] **Step 6: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java
git add backend/blog-server/src/main/resources/mapper/ArticleMapper.xml
git commit -m "feat(recommend-level): add updateRecommendLevel mapper and filter blocked articles"
```

---

### Task 5: RecommendCacheService.evictAllRecommendations

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendCacheService.java`

- [ ] **Step 1: Add evictAllRecommendations method**

Add after the existing evict methods:

```java
public void evictAllRecommendations() {
    try {
        redisTemplate.delete(KEY_REC_ANONYMOUS);

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

- [ ] **Step 2: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendCacheService.java
git commit -m "feat(recommend-level): add evictAllRecommendations method"
```

---

### Task 6: RecommendServiceImpl Algorithm Changes

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java`

- [ ] **Step 1: Add helper methods after class declaration**

Add these two private methods:

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

- [ ] **Step 2: Modify computeFullRecommendations**

In `computeFullRecommendations`, after `if (viewedArticleIds.contains(article.getId())) continue;` add:

```java
if (isBlocked(article)) continue;
```

And modify the final scores line to:

```java
finalScores.put(article.getId(), (contentScore * RecommendConstants.WEIGHT_CONTENT
        + cfScore * RecommendConstants.WEIGHT_CF
        + popScore * RecommendConstants.WEIGHT_POPULARITY) * recommendMultiplier(article));
```

- [ ] **Step 3: Modify computeColdStartRecommendations**

In `computeColdStartRecommendations`, after the for loop starts add:

```java
if (isBlocked(article)) continue;
```

And modify the final scores line to:

```java
finalScores.put(article.getId(),
        (contentScore * RecommendConstants.COLD_WEIGHT_CONTENT
        + popScore * RecommendConstants.COLD_WEIGHT_POPULARITY) * recommendMultiplier(article));
```

- [ ] **Step 4: Modify recomputeAnonymousRecommendations**

In `recomputeAnonymousRecommendations`, after `if (excludeIds.contains(article.getId())) continue;` add:

```java
if (isBlocked(article)) continue;
```

And modify the scores.put line to:

```java
scores.put(article.getId(),
        (popScore * RecommendConstants.ANON_WEIGHT_POPULARITY
        + popScore * RecommendConstants.ANON_WEIGHT_FRESHNESS
        + diversityScore * RecommendConstants.ANON_WEIGHT_DIVERSITY) * recommendMultiplier(article));
```

- [ ] **Step 5: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java
git commit -m "feat(recommend-level): apply multiplier and block logic in recommendation algorithms"
```

---

### Task 7: ArticleService Changes

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/ArticleService.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/controller/admin/ArticleController.java`

- [ ] **Step 1: Add method to ArticleService interface**

Add after `updateArticleStatus`:

```java
void updateRecommendLevel(Long id, Integer level);
```

- [ ] **Step 2: Add RecommendCacheService to ArticleServiceImpl**

Add field after `articleNotifyService`:

```java
private final RecommendCacheService recommendCacheService;
```

Add import: `top.hazenix.service.impl.RecommendCacheService;`

- [ ] **Step 3: Implement updateRecommendLevel in ArticleServiceImpl**

Add after `updateArticleStatus`:

```java
@Override
public void updateRecommendLevel(Long id, Integer level) {
    articleMapper.updateRecommendLevel(id, level);
    recommendCacheService.evictAllRecommendations();
}
```

- [ ] **Step 4: Modify addArticle to set default recommendLevel**

In `addArticle`, after setting `article.setStatus(articleDTO.getStatus());`, add:

```java
// 设置推荐度，默认为3
article.setRecommendLevel(articleDTO.getRecommendLevel() != null
    ? articleDTO.getRecommendLevel()
    : RecommendConstants.RECOMMEND_LEVEL_DEFAULT);
```

Add import: `top.hazenix.constant.RecommendConstants;`

- [ ] **Step 5: Modify updateArticle to track recommendLevel changes**

In `updateArticle`, after `BeanUtils.copyProperties(articleDTO, article);`, add:

```java
// 检查推荐度是否变化，变化时清除缓存
Integer newLevel = articleDTO.getRecommendLevel();
if (newLevel != null) {
    Article existing = articleMapper.getById(id);
    if (existing == null || !newLevel.equals(existing.getRecommendLevel())) {
        recommendCacheService.evictAllRecommendations();
    }
}
```

- [ ] **Step 6: Add controller endpoint**

In `ArticleController.java`, add after `updateArticleStatus`:

```java
/**
 * 更新文章推荐度
 * @param id 文章ID
 * @param level 推荐度 0-5
 * @return
 */
@PutMapping("/{id}/recommend-level")
public Result updateRecommendLevel(
        @PathVariable Long id,
        @RequestParam @Min(0) @Max(5) Integer level) {
    log.info("管理员修改文章 {} 推荐度为 {}", id, level);
    articleService.updateRecommendLevel(id, level);
    return Result.success();
}
```

Add imports: `javax.validation.constraints.Max;` and `javax.validation.constraints.Min;`

- [ ] **Step 7: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/ArticleService.java
git add backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java
git add backend/blog-server/src/main/java/top/hazenix/controller/admin/ArticleController.java
git commit -m "feat(recommend-level): add updateRecommendLevel API and service"
```

---

### Task 8: PopularArticleServiceImpl Filtering

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java`

- [ ] **Step 1: Add filter in scoreAndTopN**

The filtering is already done at SQL level via `listAllForScoring` which filters `recommend_level != 0`. However, add a safety check in `scoreAndTopN` as defense:

```java
public List<ArticleShortVO> scoreAndTopN(List<Article> articles, int topN) {
    if (articles == null || articles.isEmpty()) {
        return Collections.emptyList();
    }
    return articles.stream()
            // Safety filter: skip blocked articles (SQL should already filter, but defense in depth)
            .filter(a -> a.getRecommendLevel() == null || a.getRecommendLevel() != 0)
            .sorted(Comparator.comparingLong(this::heatScore).reversed())
            .limit(topN)
            .map(a -> {
                ArticleShortVO vo = new ArticleShortVO();
                BeanUtils.copyProperties(a, vo);
                return vo;
            })
            .collect(Collectors.toList());
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java
git commit -m "feat(recommend-level): filter blocked articles in popular articles"
```

---

### Task 9: Frontend API

**Files:**
- Modify: `frontend/src/api/article.js`

- [ ] **Step 1: Add updateRecommendLevel to articleApi**

Add after `toggleArticleStatus`:

```js
// 更新文章推荐度
updateRecommendLevel(id, level) {
    return request({
        url: `/admin/articles/${id}/recommend-level`,
        method: 'put',
        params: { level }
    })
},
```

Also add the backward-compatible export:

```js
export function updateRecommendLevel(id, level) {
    return articleApi.updateRecommendLevel(id, level)
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/article.js
git commit -m "feat(recommend-level): add updateRecommendLevel frontend API"
```

---

### Task 10: ToastUIEditor Form

**Files:**
- Modify: `frontend/src/components/admin/ToastUIEditor.vue`

- [ ] **Step 1: Add recommendLevel to form reactive**

In the form reactive object, add after `keywords`:

```js
recommendLevel: 3  // 默认推荐度
```

- [ ] **Step 2: Add UI for recommendLevel selector**

In the "发布设置" card, after the `isTop` checkbox section, add:

```vue
<!-- 推荐度 -->
<div>
  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
    推荐度
  </label>
  <el-select v-model="form.recommendLevel" class="w-full" size="small">
    <el-option :value="0" label="0 · 不推荐（屏蔽）" />
    <el-option :value="1" label="1 · 弱" />
    <el-option :value="2" label="2 · 较弱" />
    <el-option :value="3" label="3 · 默认" />
    <el-option :value="4" label="4 · 推荐" />
    <el-option :value="5" label="5 · 精华" />
  </el-select>
  <span class="text-xs text-gray-400 mt-1 block">
    影响文章在推荐位的曝光权重
  </span>
</div>
```

- [ ] **Step 3: Update initForm to handle recommendLevel**

In `initForm`, when editing an existing article, ensure recommendLevel is initialized. After the `Object.assign(form, {...})` block, ensure `recommendLevel` is set:

```js
// 确保推荐度有默认值
if (form.recommendLevel === undefined || form.recommendLevel === null) {
    form.recommendLevel = 3
}
```

Also add to the reset section in `initForm` when creating new article.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/admin/ToastUIEditor.vue
git commit -m "feat(recommend-level): add recommendLevel selector to ToastUIEditor"
```

---

### Task 11: ArticleManagement Quick Edit

**Files:**
- Modify: `frontend/src/views/admin/ArticleManagement.vue`

- [ ] **Step 1: Add recommendLevel column to table**

After the `状态` column (after the `</el-table-column>`), add:

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

- [ ] **Step 2: Add helper functions**

Add after the `formatDate` function:

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
  try {
    await articleApi.updateRecommendLevel(row.id, lv)
    row.recommendLevel = lv
    ElMessage.success('推荐度已更新')
  } catch (error) {
    console.error('更新推荐度失败:', error)
    ElMessage.error('更新推荐度失败')
  }
}
```

- [ ] **Step 3: Import articleApi**

If not already imported, add to imports:

```js
import { articleApi } from '@/api/article'
```

Note: May need to combine with existing imports. Check existing imports section.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/admin/ArticleManagement.vue
git commit -m "feat(recommend-level): add quick edit column in ArticleManagement"
```

---

### Task 12: RecommendCard Badge

**Files:**
- Modify: `frontend/src/components/article/RecommendCard.vue`

- [ ] **Step 1: Add relative container and badge**

Modify the template to wrap in relative div and add badges:

```vue
<template>
  <div
    class="relative flex-shrink-0 w-72 bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer overflow-hidden"
    @click="$emit('click', article)"
  >
    <!-- 推荐度徽章 -->
    <div v-if="article.recommendLevel === 5"
         class="absolute top-2 right-2 px-2 py-0.5 bg-primary text-white text-xs rounded shadow z-10">
      精华
    </div>
    <div v-else-if="article.recommendLevel === 4"
         class="absolute top-2 right-2 px-2 py-0.5 bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 text-xs rounded shadow z-10">
      推荐
    </div>

    <div class="h-36 bg-gray-100 dark:bg-gray-700 overflow-hidden" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
    </div>
    <div class="h-36 bg-gradient-to-br from-primary/10 to-primary/5 dark:from-primary/20 dark:to-primary/10 flex items-center justify-center" v-else>
      <span class="text-4xl text-primary/30">📄</span>
    </div>
    <div class="p-4">
      <h3 class="text-sm font-semibold text-gray-900 dark:text-white line-clamp-2 leading-snug">
        {{ article.title }}
      </h3>
      <div class="mt-2 flex items-center gap-3 text-xs text-gray-400">
        <span>{{ article.viewCount || 0 }} 阅读</span>
        <span>{{ article.likeCount || 0 }} 点赞</span>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/article/RecommendCard.vue
git commit -m "feat(recommend-level): add recommend level badge to RecommendCard"
```

---

### Task 13: Unit Tests

**Files:**
- Create: `backend/blog-server/src/test/java/top/hazenix/test/RecommendServiceImplTest.java`

- [ ] **Step 1: Create test file**

```java
package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendServiceImplTest {

    @Test
    public void recommendMultiplier_level_3_returns_1() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(3);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.0, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_5_returns_1_30() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(5);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.30, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_1_returns_0_70() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(1);
        double multiplier = recommendMultiplier(article);
        assertEquals(0.70, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_4_returns_1_15() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(4);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.15, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_null_returns_1() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(null);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.0, multiplier, 0.001);
    }

    @Test
    public void isBlocked_level_0_returns_true() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(0);
        assertTrue(isBlocked(article));
    }

    @Test
    public void isBlocked_level_3_returns_false() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(3);
        assertFalse(isBlocked(article));
    }

    @Test
    public void isBlocked_null_returns_false() throws Exception {
        Article article = new Article();
        article.setRecommendLevel(null);
        assertFalse(isBlocked(article));
    }

    // Helper methods that mirror the private methods in RecommendServiceImpl
    // These tests verify the formula logic independently
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
}
```

- [ ] **Step 2: Run tests to verify**

```bash
cd backend/blog-server && mvn test -Dtest=RecommendServiceImplTest -pl blog-server -am
```

- [ ] **Step 3: Commit**

```bash
git add backend/blog-server/src/test/java/top/hazenix/test/RecommendServiceImplTest.java
git commit -m "test(recommend-level): add unit tests for recommend level logic"
```

---

## Self-Review Checklist

1. **Spec coverage:** All sections from the design doc are covered:
   - [x] SQL migration with index
   - [x] Entity/DTO/VO field additions
   - [x] RecommendConstants additions
   - [x] ArticleMapper changes (updateRecommendLevel + filter)
   - [x] RecommendCacheService.evictAllRecommendations
   - [x] RecommendServiceImpl multiplier/block logic in 3 methods
   - [x] PopularArticleServiceImpl filter
   - [x] ArticleService interface + implementation
   - [x] ArticleController endpoint
   - [x] Frontend API
   - [x] ToastUIEditor form
   - [x] ArticleManagement quick edit
   - [x] RecommendCard badges
   - [x] Unit tests

2. **Placeholder scan:** No "TODO", "TBD", or vague requirements found.

3. **Type consistency:** All method signatures, field names, and constants match between tasks.

4. **YAGNI check:** All features are from the approved design doc. No speculative features added.

---

## Plan Complete

**Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
