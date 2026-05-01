package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.UserBehavior;
import top.hazenix.recommend.CollaborativeFilterEngine;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CollaborativeFilterEngineTest {

    private CollaborativeFilterEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new CollaborativeFilterEngine();
    }

    @Test
    public void buildRatingMatrix_should_take_max_score_per_user_article() {
        UserBehavior view = new UserBehavior();
        view.setUserId(1L); view.setArticleId(100L);
        view.setBehaviorType(1); view.setDuration(10); // score=1

        UserBehavior like = new UserBehavior();
        like.setUserId(1L); like.setArticleId(100L);
        like.setBehaviorType(2); // score=3

        Map<Long, Map<Long, Double>> matrix = engine.buildRatingMatrix(Arrays.asList(view, like));
        assertEquals(3.0, matrix.get(1L).get(100L));
    }

    @Test
    public void cosineSimilarity_should_compute_correctly() {
        // Article A: user1=3, user2=5
        // Article B: user1=3, user2=5
        // identical vectors -> similarity = 1.0
        Map<Long, Double> ratingsA = new HashMap<>();
        ratingsA.put(1L, 3.0); ratingsA.put(2L, 5.0);
        Map<Long, Double> ratingsB = new HashMap<>();
        ratingsB.put(1L, 3.0); ratingsB.put(2L, 5.0);

        assertEquals(1.0, engine.cosineSimilarity(ratingsA, ratingsB), 0.001);
    }

    @Test
    public void cosineSimilarity_should_return_0_when_no_common_users() {
        Map<Long, Double> ratingsA = new HashMap<>();
        ratingsA.put(1L, 3.0);
        Map<Long, Double> ratingsB = new HashMap<>();
        ratingsB.put(2L, 5.0);

        assertEquals(0.0, engine.cosineSimilarity(ratingsA, ratingsB));
    }

    @Test
    public void cfScoreForArticle_should_weight_by_similarity() {
        Map<Long, Double> userRatings = new HashMap<>();
        userRatings.put(10L, 5.0); // user rated article 10 = 5
        userRatings.put(20L, 1.0); // user rated article 20 = 1

        Map<Long, Double> candidateSim = new HashMap<>();
        candidateSim.put(10L, 0.8); // candidate similar to article 10
        candidateSim.put(20L, 0.2); // candidate less similar to article 20

        // (5*0.8 + 1*0.2) / (0.8 + 0.2) = 4.2 / 1.0 = 4.2
        double score = engine.cfScoreForArticle(userRatings, 30L, candidateSim);
        assertEquals(4.2, score, 0.001);
    }
}