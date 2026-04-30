package top.hazenix.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.hazenix.service.PopularArticleService;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularArticleTask {

    private final PopularArticleService popularArticleService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledRecompute() {
        log.info("【定时任务】开始重算热门文章");
        popularArticleService.recompute();
    }

    @PostConstruct
    public void warmUpOnStartup() {
        log.info("【启动预热】开始重算热门文章");
        try {
            popularArticleService.recompute();
        } catch (Exception e) {
            log.error("启动预热失败", e);
        }
    }
}
