package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularityEngine {

    public double score(Article article) {
        long v = article.getViewCount() == null ? 0 : article.getViewCount();
        long l = article.getLikeCount() == null ? 0 : article.getLikeCount();
        long f = article.getFavoriteCount() == null ? 0 : article.getFavoriteCount();

        double rawScore = v + l * 10.0 + f * 20.0;

        long daysSincePublish = 0;
        if (article.getCreateTime() != null) {
            daysSincePublish = ChronoUnit.DAYS.between(article.getCreateTime(), LocalDateTime.now());
        }

        return rawScore * Math.exp(-RecommendConstants.TIME_DECAY_LAMBDA * daysSincePublish);
    }
}