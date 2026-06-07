package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleUrgeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ArticleUrgeController {

    private final ArticleUrgeService urgeService;

    @GetMapping("/urge/count")
    public Result<Map<String, Integer>> getCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("currentCount", urgeService.getCurrentMonthCount());
        return Result.success(result);
    }

    @PostMapping("/urge")
    public Result<Map<String, Integer>> urge() {
        int count = urgeService.urgeAndGetCount();
        Map<String, Integer> result = new HashMap<>();
        result.put("currentCount", count);
        return Result.success(result);
    }
}