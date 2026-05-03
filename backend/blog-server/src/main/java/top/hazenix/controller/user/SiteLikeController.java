package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.SiteLikeService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SiteLikeController {

    private final SiteLikeService siteLikeService;

    @PostMapping("/site-like")
    public Result<Map<String, Long>> like(HttpServletRequest request) {
        String ipHash = getClientIpHash(request);
        try {
            long total = siteLikeService.likeAndGetCount(ipHash);
            Map<String, Long> result = new HashMap<>();
            result.put("totalCount", total);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error("409", e.getMessage());
        }
    }

    private String getClientIpHash(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "null".equals(ip)) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "null".equals(ip)) ip = request.getRemoteAddr();
        // 在请求头有多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return String.valueOf(ip.hashCode());
    }
}