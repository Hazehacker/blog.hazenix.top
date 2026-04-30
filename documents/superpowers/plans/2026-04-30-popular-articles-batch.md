# 热门文章离线批量计算 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把「热门文章」从每次请求实时查 DB 改造为「凌晨 1 点定时批量计算 → Redis 缓存 → 请求直读」。

**Architecture:** 新增 `PopularArticleService` 负责拉取所有 `status=0` 文章、按 `view*1+like*3+favorite*5` 算分、取 Top 8 写入 Redis key `popular:articles`。新增 `PopularArticleTask` 用 `@Scheduled(cron="0 0 1 * * ?")` 触发，启动时通过 `@PostConstruct` 预热一次。Controller 改为先读 Redis，miss 降级到原 SQL 并触发异步重算。

**Tech Stack:** Spring Boot 2.x, MyBatis, Spring Data Redis (`RedisTemplate`)、`@Scheduled`、JUnit 4 + Spring Boot Test + Mockito

**Spec:** [documents/superpowers/specs/2026-04-30-popular-articles-batch-design.md](../specs/2026-04-30-popular-articles-batch-design.md)

---

## File Structure

新增：

- `backend/blog-server/src/main/java/top/hazenix/task/PopularArticleTask.java` — 调度入口（cron + 启动预热）
- `backend/blog-server/src/main/java/top/hazenix/service/PopularArticleService.java` — 接口
- `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java` — 实现：算分、读写 Redis、降级
- `backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java` — 单元测试

修改：

- `backend/blog-server/src/main/java/top/hazenix/BlogApplication.java` — 加 `@EnableScheduling`
- `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java` — `/popular` 改用新 service
- `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java` — 加 `listAllForScoring()`
- `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml` — 对应 SQL
- `backend/blog-common/src/main/java/top/hazenix/constant/DefaultConstants.java` — 加 Redis key / Top N / 权重常量

保留（不动）：

- `ArticleServiceImpl#getPopularArticles` 与原 mapper SQL `getPopularArticles` —— 作为 cache miss fallback

---

## Task 1: 添加常量

**Files:**
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/DefaultConstants.java`

- [ ] **Step 1: 在 DefaultConstants 中新增 5 个常量**

打开文件，在 `private DefaultConstants()` 之前插入：

```java
    /**
     * 热门文章 Redis 缓存 key
     */
    public static final String POPULAR_ARTICLES_REDIS_KEY = "popular:articles";

    /**
     * 热门文章 Top N（实际写入 Redis 的条数）
     */
    public static final int POPULAR_ARTICLES_TOP_N = 8;

    /**
     * 热度得分权重：浏览数
     */
    public static final int POPULAR_SCORE_WEIGHT_VIEW = 1;

    /**
     * 热度得分权重：点赞数
     */
    public static final int POPULAR_SCORE_WEIGHT_LIKE = 3;

    /**
     * 热度得分权重：收藏数
     */
    public static final int POPULAR_SCORE_WEIGHT_FAVORITE = 5;
```

注意：原文件中已有的 `POPULAR_ARTICLES_COUNT = 5` 不要动，保持向后兼容。

- [ ] **Step 2: 编译通过验证**

Run: `cd backend && mvn -pl blog-common -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-common/src/main/java/top/hazenix/constant/DefaultConstants.java
git commit -m "feat(constant): add popular article batch task constants"
```

---

## Task 2: ArticleMapper 加 `listAllForScoring`

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`

- [ ] **Step 1: 在 mapper interface 末尾追加方法声明**

打开 `ArticleMapper.java`，在 `getPopularArticles` 方法下方追加：

```java
    /**
     * 获取所有正常状态(status=0)文章，仅含算分需要的字段
     * @return 文章列表
     */
    List<Article> listAllForScoring();
```

- [ ] **Step 2: 在 ArticleMapper.xml 中追加对应 SQL**

打开 `ArticleMapper.xml`，在 `getPopularArticles` 的 `<select>` 之后追加：

```xml
    <select id="listAllForScoring" resultType="top.hazenix.entity.Article">
        select id, title, cover_image, like_count, favorite_count, view_count, create_time
        from article
        where status = 0
    </select>
```

- [ ] **Step 3: 编译通过验证**

Run: `cd backend && mvn -pl blog-server -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java backend/blog-server/src/main/resources/mapper/ArticleMapper.xml
git commit -m "feat(mapper): add listAllForScoring for popular article batch task"
```

---

## Task 3: PopularArticleService 接口

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/PopularArticleService.java`

- [ ] **Step 1: 新建接口**

```java
package top.hazenix.service;

import top.hazenix.vo.ArticleShortVO;

import java.util.List;

public interface PopularArticleService {

    /**
     * 重新计算热门文章并写入 Redis（覆盖式）。
     * 由定时任务 / 启动预热 / cache miss 触发。
     * 异常自行吞掉并 log，不抛出，避免拖垮调度线程。
     */
    void recompute();

    /**
     * 获取热门文章。优先读 Redis；miss 时降级查 DB 并异步触发 recompute。
     * @param limit 返回条数上限
     */
    List<ArticleShortVO> getCachedOrFallback(int limit);
}
```

- [ ] **Step 2: 编译通过验证**

Run: `cd backend && mvn -pl blog-server -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/service/PopularArticleService.java
git commit -m "feat(service): add PopularArticleService interface"
```

---

## Task 4: PopularArticleServiceImpl —— 算分公式（TDD）

> 这一 Task 用 TDD：先写算分函数的失败测试，再写最小实现。Redis 与降级逻辑放后续 Task。

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java`
- Create: `backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java`

- [ ] **Step 1: 写失败测试（算分 + 排序 + Top N 截断 + 空列表）**

新建 `PopularArticleServiceImplTest.java`：

```java
package top.hazenix.test;

import org.junit.Before;
import org.junit.Test;
import top.hazenix.entity.Article;
import top.hazenix.service.impl.PopularArticleServiceImpl;
import top.hazenix.vo.ArticleShortVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PopularArticleServiceImplTest {

    private PopularArticleServiceImpl service;

    @Before
    public void setUp() {
        // 先用无依赖构造器写算分纯函数测试。后续 Task 会引入 mapper / redis 依赖。
        service = new PopularArticleServiceImpl();
    }

    @Test
    public void scoreAndTopN_should_apply_weighted_formula_and_sort_desc() {
        // score = view*1 + like*3 + favorite*5
        Article a = newArticle(1L, "A", 100, 0, 0);   // 100
        Article b = newArticle(2L, "B", 0, 100, 0);   // 300
        Article c = newArticle(3L, "C", 0, 0, 100);   // 500
        Article d = newArticle(4L, "D", 50, 50, 50);  // 50+150+250 = 450

        List<ArticleShortVO> result = service.scoreAndTopN(Arrays.asList(a, b, c, d), 3);

        assertEquals(3, result.size());
        assertEquals(Long.valueOf(3L), result.get(0).getId()); // C
        assertEquals(Long.valueOf(4L), result.get(1).getId()); // D
        assertEquals(Long.valueOf(2L), result.get(2).getId()); // B
    }

    @Test
    public void scoreAndTopN_should_return_empty_when_input_empty() {
        List<ArticleShortVO> result = service.scoreAndTopN(Collections.emptyList(), 8);
        assertTrue(result.isEmpty());
    }

    @Test
    public void scoreAndTopN_should_truncate_to_topN() {
        List<Article> input = Arrays.asList(
                newArticle(1L, "a", 1, 0, 0),
                newArticle(2L, "b", 2, 0, 0),
                newArticle(3L, "c", 3, 0, 0)
        );
        assertEquals(2, service.scoreAndTopN(input, 2).size());
    }

    @Test
    public void scoreAndTopN_should_handle_null_counts_as_zero() {
        Article a = new Article();
        a.setId(1L);
        a.setTitle("a");
        // viewCount/likeCount/favoriteCount 全部 null
        List<ArticleShortVO> result = service.scoreAndTopN(Collections.singletonList(a), 5);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).getId());
    }

    private Article newArticle(Long id, String title, int view, int like, int fav) {
        Article a = new Article();
        a.setId(id);
        a.setTitle(title);
        a.setViewCount(view);
        a.setLikeCount(like);
        a.setFavoriteCount(fav);
        return a;
    }
}
```

- [ ] **Step 2: 跑测试确认 FAIL（实现还没写）**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: 编译失败 —— `PopularArticleServiceImpl` 不存在

- [ ] **Step 3: 写最小实现（仅算分函数）**

新建 `PopularArticleServiceImpl.java`：

```java
package top.hazenix.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import top.hazenix.constant.DefaultConstants;
import top.hazenix.entity.Article;
import top.hazenix.vo.ArticleShortVO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PopularArticleServiceImpl implements top.hazenix.service.PopularArticleService {

    /**
     * 算分 + 排序 + Top N 截断。包级私有，便于单测。
     */
    List<ArticleShortVO> scoreAndTopN(List<Article> articles, int topN) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        return articles.stream()
                .sorted(Comparator.comparingLong(this::heatScore).reversed())
                .limit(topN)
                .map(a -> {
                    ArticleShortVO vo = new ArticleShortVO();
                    BeanUtils.copyProperties(a, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private long heatScore(Article a) {
        long v = a.getViewCount() == null ? 0 : a.getViewCount();
        long l = a.getLikeCount() == null ? 0 : a.getLikeCount();
        long f = a.getFavoriteCount() == null ? 0 : a.getFavoriteCount();
        return v * DefaultConstants.POPULAR_SCORE_WEIGHT_VIEW
                + l * DefaultConstants.POPULAR_SCORE_WEIGHT_LIKE
                + f * DefaultConstants.POPULAR_SCORE_WEIGHT_FAVORITE;
    }

    @Override
    public void recompute() {
        // 下一 Task 实现
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public java.util.List<ArticleShortVO> getCachedOrFallback(int limit) {
        // 下一 Task 实现
        throw new UnsupportedOperationException("not implemented yet");
    }
}
```

- [ ] **Step 4: 跑测试确认 PASS**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: BUILD SUCCESS, 4 tests pass

- [ ] **Step 5: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java
git commit -m "feat(popular): add weighted heat score with TopN truncation"
```

---

## Task 5: PopularArticleServiceImpl —— `recompute()` 接 Redis

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java`
- Modify: `backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java`

- [ ] **Step 1: 在测试类里加 mapper + redis mock 与 recompute 测试**

将测试类整体替换为：

```java
package top.hazenix.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import top.hazenix.entity.Article;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.service.impl.PopularArticleServiceImpl;
import top.hazenix.vo.ArticleShortVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PopularArticleServiceImplTest {

    private ArticleMapper articleMapper;
    private RedisTemplate redisTemplate;
    private ValueOperations valueOperations;
    private PopularArticleServiceImpl service;

    @Before
    public void setUp() {
        articleMapper = mock(ArticleMapper.class);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new PopularArticleServiceImpl(articleMapper, redisTemplate);
    }

    @Test
    public void scoreAndTopN_should_apply_weighted_formula_and_sort_desc() {
        Article a = newArticle(1L, 100, 0, 0);   // 100
        Article b = newArticle(2L, 0, 100, 0);   // 300
        Article c = newArticle(3L, 0, 0, 100);   // 500
        Article d = newArticle(4L, 50, 50, 50);  // 450

        List<ArticleShortVO> result = service.scoreAndTopN(Arrays.asList(a, b, c, d), 3);

        assertEquals(3, result.size());
        assertEquals(Long.valueOf(3L), result.get(0).getId());
        assertEquals(Long.valueOf(4L), result.get(1).getId());
        assertEquals(Long.valueOf(2L), result.get(2).getId());
    }

    @Test
    public void scoreAndTopN_should_return_empty_when_input_empty() {
        assertTrue(service.scoreAndTopN(Collections.emptyList(), 8).isEmpty());
    }

    @Test
    public void scoreAndTopN_should_truncate_to_topN() {
        List<Article> input = Arrays.asList(
                newArticle(1L, 1, 0, 0),
                newArticle(2L, 2, 0, 0),
                newArticle(3L, 3, 0, 0)
        );
        assertEquals(2, service.scoreAndTopN(input, 2).size());
    }

    @Test
    public void scoreAndTopN_should_handle_null_counts_as_zero() {
        Article a = new Article();
        a.setId(1L);
        List<ArticleShortVO> result = service.scoreAndTopN(Collections.singletonList(a), 5);
        assertEquals(1, result.size());
    }

    @Test
    public void recompute_should_query_db_compute_and_write_redis() {
        when(articleMapper.listAllForScoring()).thenReturn(Arrays.asList(
                newArticle(1L, 100, 0, 0),
                newArticle(2L, 0, 0, 100)
        ));

        service.recompute();

        ArgumentCaptor<List<ArticleShortVO>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOperations).set(eq("popular:articles"), captor.capture());
        List<ArticleShortVO> written = captor.getValue();
        assertEquals(Long.valueOf(2L), written.get(0).getId());
        assertEquals(Long.valueOf(1L), written.get(1).getId());
    }

    @Test
    public void recompute_should_write_empty_list_when_no_articles() {
        when(articleMapper.listAllForScoring()).thenReturn(Collections.emptyList());

        service.recompute();

        verify(valueOperations).set(eq("popular:articles"), eq(Collections.emptyList()));
    }

    @Test
    public void recompute_should_swallow_mapper_exception() {
        when(articleMapper.listAllForScoring()).thenThrow(new RuntimeException("db down"));

        service.recompute(); // should not throw

        verify(valueOperations, never()).set(any(), any());
    }

    @Test
    public void recompute_should_swallow_redis_exception() {
        when(articleMapper.listAllForScoring()).thenReturn(Collections.singletonList(newArticle(1L, 1, 1, 1)));
        org.mockito.Mockito.doThrow(new RuntimeException("redis down")).when(valueOperations).set(any(), any());

        service.recompute(); // should not throw
    }

    private Article newArticle(Long id, int view, int like, int fav) {
        Article a = new Article();
        a.setId(id);
        a.setTitle("t" + id);
        a.setViewCount(view);
        a.setLikeCount(like);
        a.setFavoriteCount(fav);
        return a;
    }
}
```

- [ ] **Step 2: 跑测试确认新增 4 个 recompute 测试 FAIL**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: 4 个 recompute_* 测试失败（其余 4 个老的 score 测试也会因构造器签名变化编译失败 → 全部 FAIL）

- [ ] **Step 3: 改写 PopularArticleServiceImpl，注入依赖并实现 recompute**

将 `PopularArticleServiceImpl.java` 整体替换为：

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.hazenix.constant.DefaultConstants;
import top.hazenix.entity.Article;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.service.PopularArticleService;
import top.hazenix.vo.ArticleShortVO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularArticleServiceImpl implements PopularArticleService {

    private final ArticleMapper articleMapper;
    private final RedisTemplate redisTemplate;

    @Override
    public void recompute() {
        try {
            List<Article> all = articleMapper.listAllForScoring();
            List<ArticleShortVO> top = scoreAndTopN(all, DefaultConstants.POPULAR_ARTICLES_TOP_N);
            try {
                redisTemplate.opsForValue().set(DefaultConstants.POPULAR_ARTICLES_REDIS_KEY, top);
                log.info("热门文章重算完成，写入 {} 条", top.size());
            } catch (Exception e) {
                log.error("热门文章写入 Redis 失败", e);
            }
        } catch (Exception e) {
            log.error("热门文章重算失败", e);
        }
    }

    @Override
    public List<ArticleShortVO> getCachedOrFallback(int limit) {
        // 下一 Task 实现
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * 算分 + 排序 + Top N 截断。包级私有，便于单测。
     */
    List<ArticleShortVO> scoreAndTopN(List<Article> articles, int topN) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        return articles.stream()
                .sorted(Comparator.comparingLong(this::heatScore).reversed())
                .limit(topN)
                .map(a -> {
                    ArticleShortVO vo = new ArticleShortVO();
                    BeanUtils.copyProperties(a, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private long heatScore(Article a) {
        long v = a.getViewCount() == null ? 0 : a.getViewCount();
        long l = a.getLikeCount() == null ? 0 : a.getLikeCount();
        long f = a.getFavoriteCount() == null ? 0 : a.getFavoriteCount();
        return v * DefaultConstants.POPULAR_SCORE_WEIGHT_VIEW
                + l * DefaultConstants.POPULAR_SCORE_WEIGHT_LIKE
                + f * DefaultConstants.POPULAR_SCORE_WEIGHT_FAVORITE;
    }
}
```

- [ ] **Step 4: 跑测试确认全部 PASS**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: BUILD SUCCESS, 8 tests pass

- [ ] **Step 5: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java
git commit -m "feat(popular): implement recompute() with redis write and exception swallowing"
```

---

## Task 6: PopularArticleServiceImpl —— `getCachedOrFallback()` + 异步回填

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java`
- Modify: `backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java`

- [ ] **Step 1: 在测试类里追加 4 个 getCachedOrFallback 测试**

在 `PopularArticleServiceImplTest` 类内、最后一个 `@Test` 方法之后追加：

```java
    @Test
    public void getCachedOrFallback_should_return_redis_when_hit() {
        ArticleShortVO v = new ArticleShortVO();
        v.setId(99L);
        when(valueOperations.get("popular:articles")).thenReturn(Collections.singletonList(v));

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(99L), result.get(0).getId());
        verify(articleMapper, never()).getPopularArticles(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    public void getCachedOrFallback_should_query_db_when_miss() {
        when(valueOperations.get("popular:articles")).thenReturn(null);
        when(articleMapper.getPopularArticles(8)).thenReturn(Collections.singletonList(newArticle(7L, 1, 1, 1)));
        when(articleMapper.listAllForScoring()).thenReturn(Collections.emptyList()); // for async recompute

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(7L), result.get(0).getId());
        verify(articleMapper, times(1)).getPopularArticles(8);
    }

    @Test
    public void getCachedOrFallback_should_query_db_when_redis_throws() {
        when(valueOperations.get("popular:articles")).thenThrow(new RuntimeException("redis down"));
        when(articleMapper.getPopularArticles(8)).thenReturn(Collections.emptyList());

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertTrue(result.isEmpty());
        verify(articleMapper, times(1)).getPopularArticles(8);
    }

    @Test
    public void getCachedOrFallback_should_return_empty_when_redis_hit_empty_list() {
        when(valueOperations.get("popular:articles")).thenReturn(Collections.emptyList());

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertTrue(result.isEmpty());
        verify(articleMapper, never()).getPopularArticles(org.mockito.ArgumentMatchers.anyInt());
    }
```

注意：`getPopularArticles` 在 `ArticleMapper` 已有；返回 `List<Article>`，所以 `getCachedOrFallback` miss 路径需要把 `Article` 转 `ArticleShortVO`。

- [ ] **Step 2: 跑测试确认 4 个新测试 FAIL**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: 4 个 getCachedOrFallback_* 测试失败（impl 仍抛 UnsupportedOperationException）

- [ ] **Step 3: 实现 getCachedOrFallback**

把 `PopularArticleServiceImpl.getCachedOrFallback` 整个方法替换为：

```java
    @Override
    public List<ArticleShortVO> getCachedOrFallback(int limit) {
        try {
            Object cached = redisTemplate.opsForValue().get(DefaultConstants.POPULAR_ARTICLES_REDIS_KEY);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<ArticleShortVO> list = (List<ArticleShortVO>) cached;
                if (list.size() <= limit) {
                    return list;
                }
                return list.subList(0, limit);
            }
        } catch (Exception e) {
            log.error("热门文章读取 Redis 失败，降级查 DB", e);
        }

        // miss / 异常 → 降级 DB + 异步回填
        List<ArticleShortVO> fallback = fallbackFromDb(limit);
        triggerAsyncRecompute();
        return fallback;
    }

    private List<ArticleShortVO> fallbackFromDb(int limit) {
        List<Article> articles = articleMapper.getPopularArticles(limit);
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        return articles.stream().map(a -> {
            ArticleShortVO vo = new ArticleShortVO();
            BeanUtils.copyProperties(a, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void triggerAsyncRecompute() {
        Thread t = new Thread(this::recompute, "popular-article-recompute");
        t.setDaemon(true);
        t.start();
    }
```

> 说明：用 `new Thread` 启动是刻意选择 —— 避免把 `@EnableAsync` 引入项目带来的连锁影响；`recompute` 已经吞了所有异常，单独一个守护线程足够。后续若引入线程池可替换。

- [ ] **Step 4: 跑测试确认全部 PASS**

Run: `cd backend && mvn -pl blog-server -Dtest=PopularArticleServiceImplTest test`
Expected: BUILD SUCCESS, 12 tests pass

- [ ] **Step 5: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java backend/blog-server/src/test/java/top/hazenix/test/PopularArticleServiceImplTest.java
git commit -m "feat(popular): implement getCachedOrFallback with db fallback and async recompute"
```

---

## Task 7: PopularArticleTask 调度器

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/task/PopularArticleTask.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/BlogApplication.java`

- [ ] **Step 1: 在 BlogApplication 加 `@EnableScheduling`**

修改 `BlogApplication.java`：

在 import 区追加：
```java
import org.springframework.scheduling.annotation.EnableScheduling;
```

在类注解块追加 `@EnableScheduling`：

```java
@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableKnife4j
@EnableSwagger2
@EnableCaching//开启缓存注解功能
@EnableScheduling // 启用定时任务
public class BlogApplication {
```

- [ ] **Step 2: 创建 PopularArticleTask**

```java
package top.hazenix.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.hazenix.service.PopularArticleService;

import javax.annotation.PostConstruct;

/**
 * 热门文章离线计算调度入口。
 * - 每天凌晨 1 点定时跑
 * - 启动时预热一次，防 Redis 冷启动空窗
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PopularArticleTask {

    private final PopularArticleService popularArticleService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledRecompute() {
        log.info("【定时任务】开始重算热门文章");
        popularArticleService.recompute();
    }

    @PostConstruct
    public void warmUpOnStartup() {
        log.info("【启动预热】开始重算热门文章");
        try {
            popularArticleService.recompute();
        } catch (Exception e) {
            // recompute 内部已吞异常，这里再加一层保险，防止启动失败
            log.error("启动预热失败", e);
        }
    }
}
```

- [ ] **Step 3: 编译通过验证**

Run: `cd backend && mvn -pl blog-server -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/BlogApplication.java backend/blog-server/src/main/java/top/hazenix/task/PopularArticleTask.java
git commit -m "feat(task): add PopularArticleTask with cron 0 0 1 * * ? and startup warmup"
```

---

## Task 8: ArticleController 切到新 service

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java`

- [ ] **Step 1: 注入 PopularArticleService 并改 `/popular`**

修改 `ArticleController.java`：

在 import 区追加：
```java
import top.hazenix.service.PopularArticleService;
```

在字段区追加（保留 `articleService` 不变）：
```java
    private final PopularArticleService popularArticleService;
```

将 `getPopularArticles` 方法替换为：

```java
    /**
     * 获取热门文章（读 Redis，miss 降级到 DB）
     * @return
     */
    @GetMapping("/popular")
    public Result getPopularArticles(){
        log.info("获取最热文章");
        List<ArticleShortVO> list = popularArticleService.getCachedOrFallback(8);
        return Result.success(list);
    }
```

- [ ] **Step 2: 编译通过验证**

Run: `cd backend && mvn -pl blog-server -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git add backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java
git commit -m "refactor(controller): popular endpoint reads from PopularArticleService cache"
```

---

## Task 9: 全量回归 + 手动验证

**Files:** —— 无新增

- [ ] **Step 1: 跑全部单元测试**

Run: `cd backend && mvn -pl blog-server test`
Expected: BUILD SUCCESS

- [ ] **Step 2: 启动应用，看日志是否打印「启动预热」与「重算完成，写入 N 条」**

Run: `cd backend && mvn -pl blog-server spring-boot:run`
Expected:
- 启动后能看到 `【启动预热】开始重算热门文章`
- 紧接着 `热门文章重算完成，写入 N 条`

- [ ] **Step 3: 用 redis-cli 检查 key**

Run: `redis-cli get popular:articles`
Expected: 返回 JSON 数组（长度 ≤ 8），每项含 `id`、`title` 等字段

- [ ] **Step 4: 调用 HTTP 接口**

Run: `curl -i http://localhost:<port>/user/articles/popular`（端口看 application.yml）
Expected: 200，body 与 Redis 中的列表一致

- [ ] **Step 5: 测 cache miss 降级**

Run:
```bash
redis-cli del popular:articles
curl http://localhost:<port>/user/articles/popular
redis-cli get popular:articles
```
Expected:
- 第二行接口仍返回数据（来自 DB fallback）
- 第三行 redis 又被异步重算填回（可能要等 1-2 秒）

- [ ] **Step 6: 验证 cron 触发（可选）**

临时把 `PopularArticleTask.scheduledRecompute` 上的 cron 改为 `0/30 * * * * ?`（每 30 秒），重启，观察是否周期性打印「定时任务」日志。验证完后改回 `0 0 1 * * ?` 并提交。

- [ ] **Step 7: 最终 commit（如有 cron 临时改动需复原）**

如果 Step 6 改过 cron，复原后：
```bash
cd d:/ProjectOfBZH/HazenixBlogProject
git diff
git add -p
git commit -m "chore: restore production cron schedule"
```
否则跳过此步。

---

## 完成标准

- [x] `redis-cli get popular:articles` 有数据
- [x] `GET /user/articles/popular` 返回与 Redis 一致
- [x] `redis-cli del popular:articles` 后接口仍可用且 Redis 被重新填充
- [x] 全部单元测试通过
- [x] cron 表达式为 `0 0 1 * * ?`
