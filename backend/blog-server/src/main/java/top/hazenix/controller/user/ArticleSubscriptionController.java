package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleSubscriptionService;

import java.util.Map;

@RestController
@RequestMapping("/user/subscription")
@RequiredArgsConstructor
public class ArticleSubscriptionController {

    private final ArticleSubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            subscriptionService.subscribe(email);
            return Result.success();
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("已订阅")) {
                return Result.error("409", msg);
            }
            return Result.error("400", msg != null ? msg : "订阅失败");
        }
    }
}