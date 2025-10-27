package top.hazenix.controller.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.CommentsDTO;
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
public class CommentsController {

    @Autowired
    private CommentsService commentsService;


    /**
     * 获取评论列表(用户端)
     */
    @GetMapping
    public Result getCommentsList(CommentsDTO commentsDTO){
        log.info("获取评论列表:{}",commentsDTO);
        List<CommentsVO> listRes = commentsService.getCommentsList(commentsDTO);
        return Result.success(listRes);
    }

    /**
     * 用户新增评论
     * @param commentsDTO
     * @return
     */
    @PostMapping
    public Result addComment(@RequestBody CommentsDTO commentsDTO){
        log.info("用户新增评论：{}",commentsDTO);
        //TODO 登录功能做出来之后再测试
        commentsService.addComments(commentsDTO);
        return Result.success();
    }


}
