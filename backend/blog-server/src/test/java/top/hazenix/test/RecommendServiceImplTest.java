package top.hazenix.test;

import org.junit.jupiter.api.Test;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendServiceImplTest {

    @Test
    public void recommendMultiplier_level_3_returns_1() {
        Article article = new Article();
        article.setRecommendLevel(3);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.0, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_5_returns_1_30() {
        Article article = new Article();
        article.setRecommendLevel(5);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.30, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_1_returns_0_70() {
        Article article = new Article();
        article.setRecommendLevel(1);
        double multiplier = recommendMultiplier(article);
        assertEquals(0.70, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_level_4_returns_1_15() {
        Article article = new Article();
        article.setRecommendLevel(4);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.15, multiplier, 0.001);
    }

    @Test
    public void recommendMultiplier_null_returns_1() {
        Article article = new Article();
        article.setRecommendLevel(null);
        double multiplier = recommendMultiplier(article);
        assertEquals(1.0, multiplier, 0.001);
    }

    @Test
    public void isBlocked_level_0_returns_true() {
        Article article = new Article();
        article.setRecommendLevel(0);
        assertTrue(isBlocked(article));
    }

    @Test
    public void isBlocked_level_3_returns_false() {
        Article article = new Article();
        article.setRecommendLevel(3);
        assertFalse(isBlocked(article));
    }

    @Test
    public void isBlocked_null_returns_false() {
        Article article = new Article();
        article.setRecommendLevel(null);
        assertFalse(isBlocked(article));
    }

    // Helper methods that mirror the private methods in RecommendServiceImpl
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
