package top.hazenix.service;

import top.hazenix.vo.ArticleShortVO;
import java.util.List;

public interface RecommendService {
    List<ArticleShortVO> getRecommendations(Long userId, int size);
    void recomputeAnonymousRecommendations();
    void recomputeContentSimilarityMatrix();
    void recomputeCFSimilarityMatrix();
    void refreshUserRecommendations(Long userId);
}