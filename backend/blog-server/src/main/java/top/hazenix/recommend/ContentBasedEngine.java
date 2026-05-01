package top.hazenix.recommend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserInterest;

import java.util.*;

@Component
@Slf4j
public class ContentBasedEngine {

    /**
     * 计算两篇文章的 Jaccard 相似度 + 同分类加分
     */
    public double articleSimilarity(Long articleA, Long articleB,
                                     Set<Integer> tagsA, Set<Integer> tagsB,
                                     Long categoryA, Long categoryB) {
        if (tagsA.isEmpty() && tagsB.isEmpty()) return 0.0;

        Set<Integer> intersection = new HashSet<>(tagsA);
        intersection.retainAll(tagsB);

        Set<Integer> union = new HashSet<>(tagsA);
        union.addAll(tagsB);

        double jaccard = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();

        if (categoryA != null && categoryA.equals(categoryB)) {
            jaccard = Math.min(1.0, jaccard + RecommendConstants.SAME_CATEGORY_BONUS);
        }
        return jaccard;
    }

    /**
     * 基于用户兴趣标签计算文章的内容推荐得分
     */
    public double scoreForUser(List<UserInterest> interests, Set<Integer> articleTags) {
        if (interests.isEmpty() || articleTags.isEmpty()) return 0.0;

        double totalWeight = 0.0;
        for (UserInterest interest : interests) {
            if (articleTags.contains(interest.getTagId().intValue())) {
                totalWeight += interest.getWeight();
            }
        }
        return totalWeight / articleTags.size();
    }
}