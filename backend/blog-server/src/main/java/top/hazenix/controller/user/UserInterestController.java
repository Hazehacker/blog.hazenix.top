package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserInterestDTO;
import top.hazenix.entity.UserInterest;
import top.hazenix.result.Result;
import top.hazenix.service.UserInterestService;

import java.util.List;

@RestController
@RequestMapping("/user/interests")
@Slf4j
@RequiredArgsConstructor
public class UserInterestController {

    private final UserInterestService userInterestService;

    @GetMapping
    public Result getInterests() {
        Long userId = BaseContext.getCurrentId();
        List<UserInterest> interests = userInterestService.getInterests(userId);
        return Result.success(interests);
    }

    @PostMapping
    public Result setInterests(@RequestBody UserInterestDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 设置兴趣标签: {}", userId, dto.getTagIds());
        userInterestService.setInterests(userId, dto.getTagIds());
        return Result.success();
    }

    @PutMapping
    public Result updateInterests(@RequestBody UserInterestDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 更新兴趣标签: {}", userId, dto.getTagIds());
        userInterestService.updateInterests(userId, dto.getTagIds());
        return Result.success();
    }
}