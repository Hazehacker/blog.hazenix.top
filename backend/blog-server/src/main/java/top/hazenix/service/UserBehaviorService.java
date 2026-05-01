package top.hazenix.service;

import top.hazenix.dto.UserBehaviorDTO;

public interface UserBehaviorService {
    void recordView(Long userId, Long articleId, Integer duration);
    void recordLike(Long userId, Long articleId);
    void recordFavorite(Long userId, Long articleId);
    int getUserBehaviorCount(Long userId);
    double getUserArticleScore(Long userId, Long articleId);
}