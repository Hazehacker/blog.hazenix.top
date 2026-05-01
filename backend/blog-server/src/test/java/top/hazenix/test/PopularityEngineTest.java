package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.Article;
import top.hazenix.recommend.PopularityEngine;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PopularityEngineTest {

    private PopularityEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new PopularityEngine();
    }

    @Test
    public void score_should_use_view_like_favorite_weights() {
        Article article = new Article();
        article.setViewCount(100L);
        article.setLikeCount(10L);
        article.setFavoriteCount(5L);
        article.setCreateTime(LocalDateTime.now());

        double score = engine.score(article);
        // 100 + 10*10 + 5*20 = 100 + 100 + 100 = 300
        assertEquals(300.0, score, 0.001);
    }

    @Test
    public void score_should_apply_time_decay() {
        Article article = new Article();
        article.setViewCount(1000L);
        article.setLikeCount(0L);
        article.setFavoriteCount(0L);
        article.setCreateTime(LocalDateTime.now().minusDays(7)); // 7 days ago

        double score = engine.score(article);
        // 1000 * e^(-0.01 * 7) = 1000 * e^-0.07 ≈ 932.40
        assertTrue(score < 1000 && score > 900);
    }

    @Test
    public void score_should_handle_null_counts() {
        Article article = new Article();
        article.setViewCount(null);
        article.setLikeCount(null);
        article.setFavoriteCount(null);
        article.setCreateTime(LocalDateTime.now());

        double score = engine.score(article);
        assertEquals(0.0, score, 0.001);
    }
}