package top.hazenix.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.SiteLikeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/site-like")
@RequiredArgsConstructor
public class SiteLikeAdminController {

    private final SiteLikeService siteLikeService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", siteLikeService.getTotalCount());
        result.put("todayCount", siteLikeService.getTodayCount());
        return Result.success(result);
    }
}