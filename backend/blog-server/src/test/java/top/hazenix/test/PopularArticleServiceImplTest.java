package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.Article;
import top.hazenix.service.impl.PopularArticleServiceImpl;
import top.hazenix.vo.ArticleShortVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PopularArticleServiceImplTest {

    private PopularArticleServiceImpl service;

    @BeforeEach
    public void setUp() {
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
