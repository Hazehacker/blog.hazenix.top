package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PopularArticleServiceImplTest {

    private ArticleMapper articleMapper;
    private RedisTemplate redisTemplate;
    private ValueOperations valueOperations;
    private PopularArticleServiceImpl service;

    @BeforeEach
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

    @org.junit.jupiter.api.Test
    public void getCachedOrFallback_should_return_redis_when_hit() {
        ArticleShortVO v = new ArticleShortVO();
        v.setId(99L);
        when(valueOperations.get("popular:articles")).thenReturn(Collections.singletonList(v));

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(99L), result.get(0).getId());
        verify(articleMapper, never()).getPopularArticles(org.mockito.ArgumentMatchers.anyInt());
    }

    @org.junit.jupiter.api.Test
    public void getCachedOrFallback_should_query_db_when_miss() throws InterruptedException {
        when(valueOperations.get("popular:articles")).thenReturn(null);
        when(articleMapper.getPopularArticles(8)).thenReturn(Collections.singletonList(newArticle(7L, 1, 1, 1)));
        when(articleMapper.listAllForScoring()).thenReturn(Collections.emptyList());

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(7L), result.get(0).getId());
        verify(articleMapper, org.mockito.Mockito.times(1)).getPopularArticles(8);
    }

    @org.junit.jupiter.api.Test
    public void getCachedOrFallback_should_query_db_when_redis_throws() {
        when(valueOperations.get("popular:articles")).thenThrow(new RuntimeException("redis down"));
        when(articleMapper.getPopularArticles(8)).thenReturn(Collections.emptyList());

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertTrue(result.isEmpty());
        verify(articleMapper, org.mockito.Mockito.times(1)).getPopularArticles(8);
    }

    @org.junit.jupiter.api.Test
    public void getCachedOrFallback_should_return_empty_when_redis_hit_empty_list() {
        when(valueOperations.get("popular:articles")).thenReturn(Collections.emptyList());

        List<ArticleShortVO> result = service.getCachedOrFallback(8);

        assertTrue(result.isEmpty());
        verify(articleMapper, never()).getPopularArticles(org.mockito.ArgumentMatchers.anyInt());
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
