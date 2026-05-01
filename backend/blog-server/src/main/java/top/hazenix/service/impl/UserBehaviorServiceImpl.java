package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.service.UserBehaviorService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public void recordView(Long userId, Long articleId, Integer duration) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_VIEW)
                .duration(duration)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    @Async
    public void recordLike(Long userId, Long articleId) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_LIKE)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    @Async
    public void recordFavorite(Long userId, Long articleId) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_FAVORITE)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public int getUserBehaviorCount(Long userId) {
        return userBehaviorMapper.countByUserId(userId);
    }

    @Override
    public double getUserArticleScore(Long userId, Long articleId) {
        List<UserBehavior> behaviors = userBehaviorMapper.getByUserIdAndArticleId(userId, articleId);
        double maxScore = 0;
        for (UserBehavior b : behaviors) {
            double score = 0;
            switch (b.getBehaviorType()) {
                case RecommendConstants.BEHAVIOR_VIEW:
                    score = (b.getDuration() != null && b.getDuration() > RecommendConstants.LONG_READ_THRESHOLD)
                            ? RecommendConstants.SCORE_VIEW_LONG : RecommendConstants.SCORE_VIEW;
                    break;
                case RecommendConstants.BEHAVIOR_LIKE:
                    score = RecommendConstants.SCORE_LIKE;
                    break;
                case RecommendConstants.BEHAVIOR_FAVORITE:
                    score = RecommendConstants.SCORE_FAVORITE;
                    break;
            }
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }
}