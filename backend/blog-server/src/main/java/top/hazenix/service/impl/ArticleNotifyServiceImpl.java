package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.entity.Article;
import top.hazenix.entity.ArticleNotifyLog;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleNotifyLogMapper;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.notify.ArticleMailRenderer;
import top.hazenix.notify.BlogMailSender;
import top.hazenix.service.ArticleNotifyService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleNotifyServiceImpl implements ArticleNotifyService {

    private final ArticleSubscriptionMapper subscriptionMapper;
    private final ArticleNotifyLogMapper notifyLogMapper;
    private final BlogMailSender mailSender;

    @Override
    @Async
    public void notifySubscribers(Article article) {
        java.util.List<ArticleSubscription> subscribers = subscriptionMapper.listActive();
        if (subscribers.isEmpty()) {
            log.info("No active subscribers for article {}", article.getId());
            return;
        }

        String subject = "【Hazenix Blog】新文章：" + article.getTitle();
        int success = 0, fail = 0;
        for (ArticleSubscription sub : subscribers) {
            try {
                String html = ArticleMailRenderer.render(
                        article.getTitle(),
                        article.getContent(),
                        article.getId(),
                        sub.getUnsubscribeToken()
                );
                mailSender.send(sub.getEmail(), subject, html);
                success++;
            } catch (Exception e) {
                fail++;
                log.error("Notify subscriber {} failed: {}", sub.getEmail(), e.getMessage());
            }
        }
        notifyLogMapper.insert(ArticleNotifyLog.builder()
                .articleId(article.getId())
                .sendTime(LocalDateTime.now())
                .successCount(success)
                .failCount(fail)
                .build());
        log.info("Article {} notified: success={} fail={}", article.getId(), success, fail);
    }
}
