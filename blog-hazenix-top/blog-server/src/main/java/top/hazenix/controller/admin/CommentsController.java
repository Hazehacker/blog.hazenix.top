package top.hazenix.controller.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.DeleteCommentsRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentShortVO;

import java.util.Collections;
import java.util.List;

@RestController("AdminCommentsController")
@RequestMapping("/admin/comments")
@Slf4j
@RequiredArgsConstructor
public class CommentsController {


    private final CommentsService commentsService;

    /**
     * 获取最新评论列表
     * @return
     */
    @GetMapping("/recent")
    public Result getRecentComments(){
        List<CommentShortVO> list = commentsService.getRecentComments(5);
        return Result.success(list);
    }


    /**
     * 分页查询获取评论列表
     * @param page
     * @param pageSize
     * @param keyword
     * @param status
     * @return
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer pageSize,
            String keyword,
            Integer status
    ){
        log.info("获取列表");
        PageResult pageResult = commentsService.pageQuery(page,pageSize,keyword,status);
        return Result.success(pageResult);
    }

    /**
     * 删除评论
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "commentsCache", allEntries = true)
    public Result deleteComment(@PathVariable Long id){
        log.info("删除评论：{}",id);
        commentsService.deleteComments(Collections.singletonList(id));

        return Result.success();
    }

    /**
     * 批量删除评论
     * @param deleteCommentsRequestDTO
     * @return
     */
    @DeleteMapping("/batch")
    @CacheEvict(cacheNames = "commentsCache", allEntries = true)
    public Result deleteComments(@RequestBody DeleteCommentsRequestDTO deleteCommentsRequestDTO){
        log.info("删除评论：{}",deleteCommentsRequestDTO);
        List<Long> ids = deleteCommentsRequestDTO.getIds();
        try {
            commentsService.deleteComments(ids);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return Result.success();
    }





}
