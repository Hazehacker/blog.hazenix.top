package top.hazenix.controller.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.TreeCommentsDTO;
import top.hazenix.entity.TreeComments;
import top.hazenix.result.Result;
import top.hazenix.service.TreeCommentsService;

import java.util.List;

@RestController("UserTreeCommentsController")
@RequestMapping("/user/tree")
@Slf4j
public class TreeCommentsController {
    @Autowired
    private TreeCommentsService treeCommentsService;
    /**
     * 获取树洞弹幕列表
     * @return
     */
    @GetMapping("/list")
    public Result getTreeComments(){
        log.info("获取树洞弹幕列表");
        //返回体和实体类这里基本没差，不加vo了
        List<TreeComments> list = treeCommentsService.listBulletScreens();
        return Result.success(list);
    }


    /**
     * 用户发送树洞弹幕
     * @param treeCommentsDTO
     * @return
     */
    @PostMapping
    public Result addTreeComments(@RequestBody TreeCommentsDTO treeCommentsDTO){
        log.info("添加树洞弹幕:{}", treeCommentsDTO);
        treeCommentsService.addTreeComments(treeCommentsDTO);
        return Result.success();
    }




}
