package top.hazenix.controller.admin;

import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.DeleteLinksRequestDTO;
import top.hazenix.dto.LinkDTO;
import top.hazenix.query.LinkQueryDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.LinkService;
import top.hazenix.entity.Link;

import java.util.List;

@RestController("AdminLinkController")
@Slf4j
@RequestMapping("/admin/links")
@ApiModel("友链相关接口")
@RequiredArgsConstructor
public class LinkController {


    private final LinkService linkService;
    /**
     * 获取所有友链列表
     * @return
     */
    @GetMapping
    public Result getLinksList(LinkQueryDTO linkQueryDTO){
        log.info("获取友链列表:{}",linkQueryDTO);
        PageResult pageResult = linkService.listLinks(linkQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取友链详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getLinkDetail(@PathVariable Long id){
        log.info("获取友链:{}",id);
        Link link = linkService.getLinkById(id);
        return Result.success(link);
    }

    /**
     * 添加友链
     * @param linkDTO
     * @return
     */
    @PostMapping
    public Result addLink(@RequestBody LinkDTO linkDTO){
        log.info("添加友链:{}",linkDTO);
        linkService.addLink(linkDTO);
        return Result.success();
    }

    /**
     * 修改友链
     * @param id
     * @param linkDTO
     * @return
     */
    @PutMapping("/{id}")
    public Result updateLink(@PathVariable Long id, @RequestBody LinkDTO linkDTO){
        log.info("更新友链:{}",linkDTO);
        linkService.updateLink(id,linkDTO);
        return Result.success();
    }

    /**
     * 更新友链状态
     * @param id
     * @return
     */
    @PutMapping("/{id}/status")
    public Result updateLinkStatus(@PathVariable Long id){
        log.info("更新友链状态:{}",id);
        linkService.updateLinkStatus(id);
        return Result.success();
    }


    /**
     * 删除友链
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteLink(@PathVariable Long id){
        log.info("删除友链:{}",id);
        linkService.deleteLink(id);
        return Result.success();
    }

    /**
     * 批量删除友链
     * @param
     * @return
     */
    @DeleteMapping("/batch")
    public Result deleteLinks(@RequestBody DeleteLinksRequestDTO deleteLinksRequestDTO){
        log.info("批量删除友链:{}",deleteLinksRequestDTO);

        linkService.deleteLinks(deleteLinksRequestDTO);
        return Result.success();
    }


}
