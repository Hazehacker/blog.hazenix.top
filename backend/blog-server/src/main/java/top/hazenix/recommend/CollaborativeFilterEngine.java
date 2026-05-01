package top.hazenix.recommend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;

import java.util.*;

@Component
@Slf4j
public class CollaborativeFilterEngine {

    /**
     * 构建用户-文章评分矩阵
     * key: userId -> (articleId -> score)
     */
    public Map<Long, Map<Long, Double>> buildRatingMatrix(List<UserBehavior> allBehaviors) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        for (UserBehavior b : allBehaviors) {
            double score = behaviorToScore(b);
            matrix.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .merge(b.getArticleId(), score, Math::max);
        }
        return matrix;
    }

    /**
     * 计算两篇文章的余弦相似度
     */
    public double cosineSimilarity(Map<Long, Double> ratingsA, Map<Long, Double> ratingsB) {
        Set<Long> commonUsers = new HashSet<>(ratingsA.keySet());
        commonUsers.retainAll(ratingsB.keySet());

        if (commonUsers.isEmpty()) return 0.0;

        double dotProduct = 0, normA = 0, normB = 0;
        for (Long userId : commonUsers) {
            double a = ratingsA.get(userId);
            double b = ratingsB.get(userId);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * 为用户生成 CF 推荐得分
     * userRatings: 该用户已交互的 articleId -> score
     * itemSimilarities: 候选文章 -> (已交互文章 -> similarity)
     */
    public double cfScoreForArticle(Map<Long, Double> userRatings,
                                     Long candidateArticleId,
                                     Map<Long, Double> candidateSimilarities) {
        double numerator = 0, denominator = 0;
        for (Map.Entry<Long, Double> entry : userRatings.entrySet()) {
            Long interactedArticleId = entry.getKey();
            double rating = entry.getValue();
            Double sim = candidateSimilarities.get(interactedArticleId);
            if (sim != null && sim > 0) {
                numerator += rating * sim;
                denominator += Math.abs(sim);
            }
        }
        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    private double behaviorToScore(UserBehavior b) {
        switch (b.getBehaviorType()) {
            case RecommendConstants.BEHAVIOR_VIEW:
                return (b.getDuration() != null && b.getDuration() > RecommendConstants.LONG_READ_THRESHOLD)
                        ? RecommendConstants.SCORE_VIEW_LONG : RecommendConstants.SCORE_VIEW;
            case RecommendConstants.BEHAVIOR_LIKE:
                return RecommendConstants.SCORE_LIKE;
            case RecommendConstants.BEHAVIOR_FAVORITE:
                return RecommendConstants.SCORE_FAVORITE;
            default:
                return 0;
        }
    }
}