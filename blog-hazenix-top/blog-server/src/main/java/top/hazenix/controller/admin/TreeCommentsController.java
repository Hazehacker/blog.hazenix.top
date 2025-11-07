package top.hazenix.controller.admin;

import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.DeleteCommentsRequestDTO;
import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.entity.Link;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.query.TreeCommentsQuery;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.LinkService;
import top.hazenix.service.TreeCommentsService;

import java.util.Collections;
import java.util.List;

@RestController("AdminTreeCommentsController")
@Slf4j
@RequestMapping("/admin/tree")
@ApiModel("树洞相关接口")
@RequiredArgsConstructor
public class TreeCommentsController {


    private final TreeCommentsService treeCommentsService;
    /**
     * 分页查询树洞列表
     * @param treeCommentsQuery
     * @return
     */
    @GetMapping
    public Result<PageResult> pageQuery(TreeCommentsQuery treeCommentsQuery) {
        log.info("分页查询树洞列表");
        PageResult pageResult = treeCommentsService.pageQuery(treeCommentsQuery);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    public Result deleteTreeComment(@PathVariable Long id){
        log.info("删除弹幕：{}",id);
        treeCommentsService.deleteTreeComments(Collections.singletonList(id));
        return Result.success();
    }


    /**
     * 批量删除弹幕
     * @param deleteTreeCommentsRequestDTO
     * @return
     */
    @DeleteMapping("/batch")
    public Result deleteTreeComments(DeleteCommentsRequestDTO deleteTreeCommentsRequestDTO){
        log.info("删除弹幕：{}",deleteTreeCommentsRequestDTO);
        List<Long> ids = deleteTreeCommentsRequestDTO.getIds();
        treeCommentsService.deleteTreeComments(ids);
        return Result.success();
    }

}
