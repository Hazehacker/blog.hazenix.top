package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;
import top.hazenix.entity.UserBehavior;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.ArticleTagsMapper;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.recommend.CollaborativeFilterEngine;
import top.hazenix.recommend.ContentBasedEngine;
import top.hazenix.recommend.PopularityEngine;
import top.hazenix.recommend.RecommendCacheService;
import top.hazenix.service.PopularArticleService;
import top.hazenix.service.RecommendService;
import top.hazenix.service.UserBehaviorService;
import top.hazenix.service.UserInterestService;
import top.hazenix.vo.ArticleShortVO;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final ContentBasedEngine contentEngine;
    private final CollaborativeFilterEngine cfEngine;
    private final PopularityEngine popularityEngine;
    private final RecommendCacheService cacheService;
    private final UserBehaviorService behaviorService;
    private final UserInterestService interestService;
    private final PopularArticleService popularArticleService;
    private final ArticleMapper articleMapper;
    private final ArticleTagsMapper articleTagsMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public List<ArticleShortVO> getRecommendations(Long userId, int size) {
        if (userId == null) {
            return getAnonymousRecommendations(size);
        }

        // Try cache first
        List<Long> cached = cacheService.getUserRecommendations(userId, size);
        if (!cached.isEmpty()) {
            return toArticleVOs(cached);
        }

        // Check cold start
        int behaviorCount = behaviorService.getUserBehaviorCount(userId);
        boolean hasInterests = interestService.hasInterests(userId);

        List<Long> articleIds;
        if (behaviorCount >= RecommendConstants.COLD_START_THRESHOLD) {
            articleIds = computeFullRecommendations(userId, size);
        } else if (hasInterests) {
            articleIds = computeColdStartRecommendations(userId, size);
        } else {
            return getAnonymousRecommendations(size);
        }

        return toArticleVOs(articleIds);
    }

    private List<ArticleShortVO> getAnonymousRecommendations(int size) {
        List<Long> cached = cacheService.getAnonymousRecommendations(size);
        if (!cached.isEmpty()) {
            return toArticleVOs(cached);
        }
        // Fallback to popular articles
        return popularArticleService.getCachedOrFallback(size);
    }

    private List<Long> computeFullRecommendations(Long userId, int size) {
        List<Article> allArticles = articleMapper.listAllForScoring();
        List<UserInterest> interests = interestService.getInterests(userId);
        List<UserBehavior> userBehaviors = userBehaviorMapper.getByUserId(userId);

        Set<Long> viewedArticleIds = userBehaviors.stream()
                .map(UserBehavior::getArticleId)
                .collect(Collectors.toSet());

        // Build user ratings
        Map<Long, Double> userRatings = new HashMap<>();
        for (UserBehavior b : userBehaviors) {
            double score = behaviorService.getUserArticleScore(userId, b.getArticleId());
            userRatings.merge(b.getArticleId(), score, Math::max);
        }

        // Get popular article IDs to exclude from anonymous recommendations
        List<ArticleShortVO> popularList = popularArticleService.getCachedOrFallback(5);
        Set<Long> popularIds = popularList.stream()
                .map(ArticleShortVO::getId)
                .collect(Collectors.toSet());

        Map<Long, Double> finalScores = new HashMap<>();

        for (Article article : allArticles) {
            if (viewedArticleIds.contains(article.getId())) continue;

            // Content score
            Set<Integer> articleTags = new HashSet<>(articleTagsMapper.getListByArticleId(article.getId()));
            double contentScore = contentEngine.scoreForUser(interests, articleTags);

            // CF score
            Map<Long, Double> cfSim = cacheService.getCFSimilarity(article.getId());
            double cfScore = cfEngine.cfScoreForArticle(userRatings, article.getId(), cfSim);

            // Popularity score
            double popScore = popularityEngine.score(article);

            finalScores.put(article.getId(), contentScore * RecommendConstants.WEIGHT_CONTENT
                    + cfScore * RecommendConstants.WEIGHT_CF
                    + popScore * RecommendConstants.WEIGHT_POPULARITY);
        }

        // Normalize and get top N
        List<Long> result = normalizeAndTopN(finalScores, size);

        // Cache
        Map<Long, Double> cacheMap = new LinkedHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            cacheMap.put(result.get(i), (double)(result.size() - i));
        }
        cacheService.cacheUserRecommendations(userId, cacheMap);

        return result;
    }

    private List<Long> computeColdStartRecommendations(Long userId, int size) {
        List<Article> allArticles = articleMapper.listAllForScoring();
        List<UserInterest> interests = interestService.getInterests(userId);

        Map<Long, Double> finalScores = new HashMap<>();
        for (Article article : allArticles) {
            Set<Integer> articleTags = new HashSet<>(articleTagsMapper.getListByArticleId(article.getId()));
            double contentScore = contentEngine.scoreForUser(interests, articleTags);
            double popScore = popularityEngine.score(article);

            finalScores.put(article.getId(),
                    contentScore * RecommendConstants.COLD_WEIGHT_CONTENT
                    + popScore * RecommendConstants.COLD_WEIGHT_POPULARITY);
        }

        List<Long> result = normalizeAndTopN(finalScores, size);

        Map<Long, Double> cacheMap = new LinkedHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            cacheMap.put(result.get(i), (double)(result.size() - i));
        }
        cacheService.cacheColdStartRecommendations(userId, cacheMap);

        return result;
    }

    @Override
    public void recomputeAnonymousRecommendations() {
        log.info("开始重算匿名用户推荐");
        List<Article> allArticles = articleMapper.listAllForScoring();

        // Get popular top 5 IDs to exclude
        List<ArticleShortVO> popularList = popularArticleService.getCachedOrFallback(5);
        Set<Long> excludeIds = popularList.stream()
                .map(ArticleShortVO::getId)
                .collect(Collectors.toSet());

        Map<Long, Double> scores = new HashMap<>();
        for (Article article : allArticles) {
            if (excludeIds.contains(article.getId())) continue;
            double popScore = popularityEngine.score(article);
            // Diversity: count distinct tags
            int tagCount = articleTagsMapper.getListByArticleId(article.getId()).size();
            double diversityScore = Math.min(1.0, tagCount / 5.0);

            scores.put(article.getId(),
                    popScore * RecommendConstants.ANON_WEIGHT_POPULARITY
                    + popScore * RecommendConstants.ANON_WEIGHT_FRESHNESS // freshness already in popScore via decay
                    + diversityScore * RecommendConstants.ANON_WEIGHT_DIVERSITY);
        }

        Map<Long, Double> topScores = new LinkedHashMap<>();
        normalizeAndTopN(scores, RecommendConstants.RECOMMEND_SIZE)
                .forEach(id -> topScores.put(id, scores.get(id)));

        cacheService.cacheAnonymousRecommendations(topScores);
        log.info("匿名用户推荐重算完成，共 {} 条", topScores.size());
    }

    @Override
    public void recomputeContentSimilarityMatrix() {
        log.info("开始重算内容相似度矩阵");
        List<Article> allArticles = articleMapper.listAllForScoring();
        Map<Long, Set<Integer>> articleTagsMap = new HashMap<>();
        Map<Long, Long> articleCategoryMap = new HashMap<>();

        for (Article a : allArticles) {
            articleTagsMap.put(a.getId(), new HashSet<>(articleTagsMapper.getListByArticleId(a.getId())));
            articleCategoryMap.put(a.getId(), a.getCategoryId());
        }

        for (Article a : allArticles) {
            Map<Long, Double> similarities = new HashMap<>();
            for (Article b : allArticles) {
                if (a.getId().equals(b.getId())) continue;
                double sim = contentEngine.articleSimilarity(
                        a.getId(), b.getId(),
                        articleTagsMap.get(a.getId()), articleTagsMap.get(b.getId()),
                        articleCategoryMap.get(a.getId()), articleCategoryMap.get(b.getId()));
                if (sim > 0) {
                    similarities.put(b.getId(), sim);
                }
            }
            cacheService.cacheContentSimilarity(a.getId(), similarities);
        }
        log.info("内容相似度矩阵重算完成");
    }

    @Override
    public void recomputeCFSimilarityMatrix() {
        log.info("开始重算协同过滤相似度矩阵");
        List<UserBehavior> allBehaviors = userBehaviorMapper.listAll();
        Map<Long, Map<Long, Double>> ratingMatrix = cfEngine.buildRatingMatrix(allBehaviors);

        // Transpose: articleId -> (userId -> score)
        Map<Long, Map<Long, Double>> articleRatings = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> userEntry : ratingMatrix.entrySet()) {
            Long userId = userEntry.getKey();
            for (Map.Entry<Long, Double> articleEntry : userEntry.getValue().entrySet()) {
                articleRatings.computeIfAbsent(articleEntry.getKey(), k -> new HashMap<>())
                        .put(userId, articleEntry.getValue());
            }
        }

        List<Long> articleIds = new ArrayList<>(articleRatings.keySet());
        for (int i = 0; i < articleIds.size(); i++) {
            Long articleA = articleIds.get(i);
            if (userBehaviorMapper.countByArticleId(articleA) < RecommendConstants.CF_MIN_BEHAVIOR_COUNT) {
                continue;
            }
            Map<Long, Double> similarities = new HashMap<>();
            for (int j = 0; j < articleIds.size(); j++) {
                if (i == j) continue;
                Long articleB = articleIds.get(j);
                double sim = cfEngine.cosineSimilarity(
                        articleRatings.get(articleA), articleRatings.get(articleB));
                if (sim > 0) {
                    similarities.put(articleB, sim);
                }
            }
            cacheService.cacheCFSimilarity(articleA, similarities);
        }
        log.info("协同过滤相似度矩阵重算完成");
    }

    @Override
    @Async
    public void refreshUserRecommendations(Long userId) {
        log.info("异步刷新用户 {} 的推荐缓存", userId);
        cacheService.evictUserRecommendations(userId);
        int behaviorCount = behaviorService.getUserBehaviorCount(userId);
        boolean hasInterests = interestService.hasInterests(userId);

        if (behaviorCount >= RecommendConstants.COLD_START_THRESHOLD) {
            computeFullRecommendations(userId, RecommendConstants.RECOMMEND_SIZE);
        } else if (hasInterests) {
            computeColdStartRecommendations(userId, RecommendConstants.RECOMMEND_SIZE);
        }
    }

    private List<Long> normalizeAndTopN(Map<Long, Double> scores, int topN) {
        if (scores.isEmpty()) return Collections.emptyList();

        double min = Collections.min(scores.values());
        double max = Collections.max(scores.values());
        double range = max - min;

        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(
                        range == 0 ? 0 : (b.getValue() - min) / range,
                        range == 0 ? 0 : (a.getValue() - min) / range))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<ArticleShortVO> toArticleVOs(List<Long> articleIds) {
        if (articleIds.isEmpty()) return Collections.emptyList();
        List<ArticleShortVO> result = new ArrayList<>();
        for (Long id : articleIds) {
            Article article = articleMapper.getById(id);
            if (article != null) {
                ArticleShortVO vo = new ArticleShortVO();
                BeanUtils.copyProperties(article, vo);
                result.add(vo);
            }
        }
        return result;
    }
}