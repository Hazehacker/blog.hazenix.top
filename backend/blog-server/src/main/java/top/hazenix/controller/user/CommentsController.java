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
import top.hazenix.dto.CommentsDTO;
import top.hazenix.exception.BussinessException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import top.hazenix.result.Result;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentsVO;

import java.util.List;

@RestController("UserCommentsController")
@RequestMapping("/user/comments")
@Slf4j
@RequiredArgsConstructor
public class CommentsController {


    private final CommentsService commentsService;
    private final CommentRateLimiter commentRateLimiter;


    @GetMapping("/list")
    @Cacheable(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result<List<CommentsVO>> getCommentsList(CommentsDTO commentsDTO){
        log.info("获取评论列表:{}",commentsDTO);
        List<CommentsVO> commentTree = commentsService.getCommentsList(commentsDTO);
        return Result.success(commentTree);
    }

    @PostMapping
    @CacheEvict(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result addComment(@Valid @RequestBody CommentsDTO commentsDTO, HttpServletRequest request){
        log.info("用户新增评论：{}",commentsDTO);

        if (BaseContext.getCurrentId() == null) {
            String ip = getClientIp(request);
            if (!commentRateLimiter.isAllowed(ip)) {
                throw new BussinessException(ErrorCode.A03003, MessageConstant.COMMENT_RATE_LIMIT_EXCEEDED);
            }
        }

        commentsService.addComments(commentsDTO);
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
