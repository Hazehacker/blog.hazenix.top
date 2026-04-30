package top.hazenix.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyConfigService;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class NotifyScheduleManager {

    private final NotifyConfigService notifyConfigService;
    private final DailyNotifyService dailyNotifyService;
    private final TaskScheduler taskScheduler;

    private volatile ScheduledFuture<?> currentFuture;

    public NotifyScheduleManager(NotifyConfigService notifyConfigService,
                                  DailyNotifyService dailyNotifyService,
                                  TaskScheduler taskScheduler) {
        this.notifyConfigService = notifyConfigService;
        this.dailyNotifyService = dailyNotifyService;
        this.taskScheduler = taskScheduler;
    }

    @Configuration
    static class SchedulerConfig {
        @Bean
        public TaskScheduler notifyTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(2);
            scheduler.setThreadNamePrefix("notify-");
            scheduler.initialize();
            return scheduler;
        }
    }

    @PostConstruct
    public void init() {
        reschedule();
    }

    public synchronized void reschedule() {
        if (currentFuture != null) {
            currentFuture.cancel(false);
            currentFuture = null;
        }
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null || config.getEnabled() == 0) {
            log.info("Notify scheduling disabled");
            return;
        }
        scheduleNext(config.getSendTime());
    }

    private void scheduleNext(String sendTime) {
        String[] parts = sendTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        LocalDateTime nextRun = LocalDate.now().atTime(hour, minute);
        if (!nextRun.isAfter(LocalDateTime.now())) {
            nextRun = nextRun.plusDays(1);
        }

        Instant runInstant = nextRun.atZone(ZoneId.systemDefault()).toInstant();
        currentFuture = taskScheduler.schedule(() -> {
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                dailyNotifyService.runForDate(yesterday);
            } catch (Exception e) {
                log.error("Notify task execution error", e);
            } finally {
                NotifyConfig latest = notifyConfigService.getRawConfig();
                if (latest != null && latest.getEnabled() == 1) {
                    scheduleNext(latest.getSendTime());
                }
            }
        }, runInstant);

        log.info("Next notify scheduled at {}", nextRun);
    }
}
