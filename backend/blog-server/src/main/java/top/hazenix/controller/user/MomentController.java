package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.MomentService;
import top.hazenix.vo.MomentVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController("UserMomentController")
@RequestMapping("/user/moment")
@Slf4j
@RequiredArgsConstructor
public class MomentController {

    private final MomentService momentService;

    @GetMapping("/page")
    public Result<PageResult> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagName,
            HttpServletRequest request) {
        log.info("获取手记分页列表, tagName={}", tagName);
        String ip = getClientIp(request);
        PageResult result = momentService.pageQueryUser(page, pageSize, tagName, ip);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MomentVO> detail(@PathVariable Long id, HttpServletRequest request) {
        log.info("获取手记详情, id={}", id);
        String ip = getClientIp(request);
        MomentVO vo = momentService.getById(id, ip);
        return Result.success(vo);
    }

    @PostMapping("/{id}/like")
    public Result like(@PathVariable Long id, HttpServletRequest request) {
        log.info("手记点赞, id={}", id);
        String ip = getClientIp(request);
        momentService.likeMoment(id, ip);
        return Result.success();
    }

    @GetMapping("/tags")
    public Result<List<String>> tags() {
        log.info("获取手记标签列表");
        return Result.success(momentService.getAllTags());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
