package top.hazenix.controller.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;

import java.util.List;

@RestController("UserCommentsController")
@RequestMapping("/user/comments")
@Slf4j
public class CommentsController {




}
