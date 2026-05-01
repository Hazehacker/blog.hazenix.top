package top.hazenix.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.hazenix.service.RecommendService;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendTask {

    private final RecommendService recommendService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void recomputeCFMatrix() {
        log.info("【定时任务】开始重算协同过滤相似度矩阵");
        try {
            recommendService.recomputeCFSimilarityMatrix();
        } catch (Exception e) {
            log.error("协同过滤矩阵重算失败", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void recomputeContentMatrix() {
        log.info("【定时任务】开始重算内容相似度矩阵");
        try {
            recommendService.recomputeContentSimilarityMatrix();
        } catch (Exception e) {
            log.error("内容相似度矩阵重算失败", e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void refreshAnonymousRecommendations() {
        log.info("【定时任务】刷新匿名用户推荐");
        try {
            recommendService.recomputeAnonymousRecommendations();
        } catch (Exception e) {
            log.error("匿名推荐刷新失败", e);
        }
    }

    @PostConstruct
    public void warmUpOnStartup() {
        log.info("【启动预热】初始化推荐系统缓存");
        try {
            recommendService.recomputeAnonymousRecommendations();
            recommendService.recomputeContentSimilarityMatrix();
        } catch (Exception e) {
            log.error("推荐系统启动预热失败", e);
        }
    }
}
