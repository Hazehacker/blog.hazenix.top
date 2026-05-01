package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserBehaviorDTO;
import top.hazenix.result.Result;
import top.hazenix.service.RecommendService;
import top.hazenix.service.UserBehaviorService;

@RestController
@RequestMapping("/user/behavior")
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;
    private final RecommendService recommendService;

    @PostMapping
    public Result reportBehavior(@RequestBody UserBehaviorDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.success(); // 匿名用户不记录
        }
        log.info("用户 {} 上报浏览行为: articleId={}, duration={}", userId, dto.getArticleId(), dto.getDuration());
        userBehaviorService.recordView(userId, dto.getArticleId(), dto.getDuration());
        recommendService.refreshUserRecommendations(userId);
        return Result.success();
    }
}