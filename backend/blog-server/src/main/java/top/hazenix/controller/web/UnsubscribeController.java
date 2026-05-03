package top.hazenix.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import top.hazenix.service.ArticleSubscriptionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UnsubscribeController {

    private final ArticleSubscriptionService subscriptionService;

    @GetMapping(value = "/unsubscribe", produces = MediaType.TEXT_HTML_VALUE)
    public String unsubscribe(@RequestParam String token) {
        try {
            subscriptionService.unsubscribe(token);
        } catch (Exception e) {
            return "<html><body><h2>退订失败</h2><p>链接无效或已过期。</p></body></html>";
        }
        return "<html><body><h2>退订成功</h2><p>已取消文章订阅通知。</p></body></html>";
    }
}