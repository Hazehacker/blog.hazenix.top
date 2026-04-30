package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.entity.NotifyLog;
import top.hazenix.entity.TreeComments;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.mapper.NotifyLogMapper;
import top.hazenix.mapper.TreeCommentsMapper;
import top.hazenix.notify.BlogMailSender;
import top.hazenix.notify.NotifyHtmlRenderer;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyActionService;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.vo.CommentNotifyVO;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNotifyServiceImpl implements DailyNotifyService {

    private final CommentsMapper commentsMapper;
    private final TreeCommentsMapper treeCommentsMapper;
    private final LinkMapper linkMapper;
    private final NotifyLogMapper notifyLogMapper;
    private final NotifyConfigService notifyConfigService;
    private final NotifyActionService notifyActionService;
    private final BlogMailSender blogMailSender;
    private final TaskScheduler taskScheduler;

    @Value("${blog.notify.public-base-url}")
    private String publicBaseUrl;

    @Override
    public void runForDate(LocalDate statDate) {
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null || config.getEnabled() == 0) {
            log.info("Notify disabled or not configured, skip");
            return;
        }

        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();

        List<CommentNotifyVO> comments = commentsMapper.listByCreateTimeBetween(start, end);
        List<TreeComments> treeComments = treeCommentsMapper.listByCreateTimeBetween(start, end);
        List<Link> pendingLinks = linkMapper.listPendingByCreateTimeBetween(start, end);

        Map<Long, String[]> linkTokens = new HashMap<>();
        for (Link link : pendingLinks) {
            String approveToken = notifyActionService.issueToken(link.getId(), "link", "approve");
            String rejectToken = notifyActionService.issueToken(link.getId(), "link", "reject");
            linkTokens.put(link.getId(), new String[]{approveToken, rejectToken});
        }

        String subject = "【Hazenix Blog】每日通知 " + statDate;
        String html = NotifyHtmlRenderer.render(statDate, comments, treeComments, pendingLinks, linkTokens, publicBaseUrl);

        sendWithRetry(config.getRecipient(), subject, html, statDate, 0);
    }

    @Override
    public void sendTestMail() {
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null) {
            throw new RuntimeException("通知配置不存在");
        }
        String html = "<!DOCTYPE html><html><body><h2>Hazenix Blog 邮件测试</h2>"
                + "<p>如果你收到这封邮件，说明 SMTP 配置正确。</p>"
                + "<p style='color:#999'>发送时间：" + LocalDateTime.now() + "</p></body></html>";
        blogMailSender.send(config.getRecipient(), "【Hazenix Blog】测试邮件", html);
    }

    private void sendWithRetry(String recipient, String subject, String html, LocalDate statDate, int attempt) {
        try {
            blogMailSender.send(recipient, subject, html);
            notifyLogMapper.insert(NotifyLog.builder()
                    .statDate(statDate)
                    .sendTime(LocalDateTime.now())
                    .status(0)
                    .retryCount(attempt)
                    .recipient(recipient)
                    .build());
            log.info("Daily notify sent for {} attempt={}", statDate, attempt);
        } catch (Exception e) {
            log.error("Daily notify failed for {} attempt={}: {}", statDate, attempt, e.getMessage());
            if (attempt < NotifyConstants.RETRY_DELAY_MINUTES.length) {
                int delayMinutes = NotifyConstants.RETRY_DELAY_MINUTES[attempt];
                Instant retryAt = Instant.now().plusSeconds(delayMinutes * 60L);
                taskScheduler.schedule(
                        () -> sendWithRetry(recipient, subject, html, statDate, attempt + 1),
                        retryAt
                );
            } else {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 2000) {
                    errorMsg = errorMsg.substring(0, 2000);
                }
                notifyLogMapper.insert(NotifyLog.builder()
                        .statDate(statDate)
                        .sendTime(LocalDateTime.now())
                        .status(1)
                        .retryCount(attempt)
                        .errorMsg(errorMsg)
                        .recipient(recipient)
                        .build());
            }
        }
    }
}
