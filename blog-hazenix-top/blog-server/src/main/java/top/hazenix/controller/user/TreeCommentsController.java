package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.TreeCommentsDTO;

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
    /**
     * 获取树洞弹幕列表
     * @return
     */
    @GetMapping("/list")
    @Cacheable(cacheNames = "treeCache", key = "#root.method")
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
    @CacheEvict(cacheNames = "treeCache", allEntries = true)
    public Result addTreeComments(@Valid @RequestBody TreeCommentsDTO treeCommentsDTO){
        log.info("添加树洞弹幕:{}", treeCommentsDTO);
        treeCommentsService.addTreeComments(treeCommentsDTO);
        return Result.success();
    }




}
