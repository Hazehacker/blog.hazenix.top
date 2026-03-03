package top.hazenix.controller.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.CommentsDTO;

import javax.validation.Valid;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;
import top.hazenix.vo.CommentsVO;

import java.util.List;

@RestController("UserCommentsController")
@RequestMapping("/user/comments")
@Slf4j
@RequiredArgsConstructor
public class CommentsController {


    private final CommentsService commentsService;


    /**
     * 获取评论列表(用户端)
     */
    @GetMapping("/list")
    @Cacheable(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result<List<CommentsVO>> getCommentsList(CommentsDTO commentsDTO){
        log.info("获取评论列表:{}",commentsDTO);
        List<CommentsVO> commentTree = commentsService.getCommentsList(commentsDTO);
        return Result.success(commentTree);
    }

    /**
     * 用户新增评论
     * @param commentsDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result addComment(@Valid @RequestBody CommentsDTO commentsDTO){
        log.info("用户新增评论：{}",commentsDTO);
        commentsService.addComments(commentsDTO);
        return Result.success();
    }


}
