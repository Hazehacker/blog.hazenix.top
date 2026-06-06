package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.config.CommentRateLimiter;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.MessageConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.TreeCommentsDTO;
import top.hazenix.exception.BussinessException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import top.hazenix.entity.TreeComments;
import top.hazenix.result.Result;
import top.hazenix.service.TreeCommentsService;

import java.util.List;

@RestController("UserTreeCommentsController")
@RequestMapping("/user/tree")
@Slf4j
@RequiredArgsConstructor
public class TreeCommentsController {

    private final TreeCommentsService treeCommentsService;
    private final CommentRateLimiter commentRateLimiter;

    /**
     * 获取树洞弹幕列表
     * @return
     */
    @GetMapping("/list")
    @Cacheable(cacheNames = "treeCache", key = "#root.method")
    public Result getTreeComments(){
        log.info("获取树洞弹幕列表");
        List<TreeComments> list = treeCommentsService.listBulletScreens();
        return Result.success(list);
    }


    /**
     * 用户发送树洞弹幕（支持匿名）
     * @param treeCommentsDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "treeCache", allEntries = true)
    public Result addTreeComments(@Valid @RequestBody TreeCommentsDTO treeCommentsDTO,
                                   HttpServletRequest request){
        log.info("添加树洞弹幕:{}", treeCommentsDTO);

        if (BaseContext.getCurrentId() == null) {
            String ip = getClientIp(request);
            if (!commentRateLimiter.isAllowed(ip)) {
                throw new BussinessException(ErrorCode.A03003, MessageConstant.COMMENT_RATE_LIMIT_EXCEEDED);
            }
            treeCommentsService.addTreeComments(treeCommentsDTO, ip);
        } else {
            treeCommentsService.addTreeComments(treeCommentsDTO, null);
        }

        return Result.success();
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
