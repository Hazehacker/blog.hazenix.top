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

    /**
     * 算分 + 排序 + Top N 截断。public 以便测试类跨包访问。
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
}
