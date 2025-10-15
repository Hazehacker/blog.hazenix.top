package top.hazenix.controller.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.result.Result;

@RestController
@RequestMapping("/user/comment")
@Slf4j
public class CommentsController {

    @GetMapping("/list/{id}")
    public Result list(){

        List<CommentsVO> list = CommentsService.list();
        return Result.success(list);
    }

}
