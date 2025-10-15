package top.hazenix.controller.admin;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.result.Result;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@Slf4j
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 获取最新评论列表
     * @return
     */
    @GetMapping("/recent")
    public Result getRecentComments(){
        List<CommentShortVO> list = commentsService.getRecentComments(5);
        return Result.success(list);
    }



//    @GetMapping("/list/{id}")
////    public Result list(){
////
////        List<CommentsVO> list = CommentsService.list();
////        return Result.success(list);
////    }

}
