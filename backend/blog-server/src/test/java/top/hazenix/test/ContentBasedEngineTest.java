package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.UserInterest;
import top.hazenix.recommend.ContentBasedEngine;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ContentBasedEngineTest {

    private ContentBasedEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new ContentBasedEngine();
    }

    @Test
    public void jaccard_should_compute_correctly() {
        // {Java, Redis, Spring} vs {Java, Spring, MySQL}
        // intersection=2, union=4, jaccard=0.5
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 3, 4));
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 2L);
        assertEquals(0.5, sim, 0.001);
    }

    @Test
    public void jaccard_should_add_category_bonus() {
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 3, 4));
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 1L); // same category
        assertEquals(0.7, sim, 0.001);
    }

    @Test
    public void jaccard_should_cap_at_1() {
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 2, 3)); // identical
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 1L);
        assertEquals(1.0, sim, 0.001);
    }

    @Test
    public void scoreForUser_should_match_interest_tags() {
        UserInterest i1 = UserInterest.builder().tagId(1L).weight(0.8).build();
        UserInterest i2 = UserInterest.builder().tagId(2L).weight(0.6).build();
        UserInterest i3 = UserInterest.builder().tagId(3L).weight(0.4).build();

        Set<Integer> articleTags = new HashSet<>(Arrays.asList(1, 3, 5));
        // matches: tag1(0.8) + tag3(0.4) = 1.2 / 3 tags = 0.4
        double score = engine.scoreForUser(Arrays.asList(i1, i2, i3), articleTags);
        assertEquals(0.4, score, 0.001);
    }

    @Test
    public void scoreForUser_should_return_0_when_no_match() {
        UserInterest i1 = UserInterest.builder().tagId(1L).weight(0.8).build();
        Set<Integer> articleTags = new HashSet<>(Arrays.asList(5, 6));
        assertEquals(0.0, engine.scoreForUser(Collections.singletonList(i1), articleTags));
    }
}