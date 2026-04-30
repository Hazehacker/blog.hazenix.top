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
     * 算分 + 排序 + Top N 截断。便于单测。
     */
    public List<ArticleShortVO> scoreAndTopN(List<Article> articles, int topN) {
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
