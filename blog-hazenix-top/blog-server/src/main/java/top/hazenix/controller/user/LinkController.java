package top.hazenix.controller.user;

import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.LinkDTO;
import top.hazenix.entity.Link;
import top.hazenix.result.Result;
import top.hazenix.service.LinkService;

import java.util.List;

@RestController("UserLinkController")
@Slf4j
@RequestMapping("/api/user/links")
@ApiModel("友链相关接口")
public class LinkController {
    @Autowired
    private LinkService LinkService;

    /**
     * 获取友链列表
     * @return
     */
    @GetMapping
    public Result getLinks(){
        log.info("获取友链列表");
        List<Link> list = LinkService.listLinksUserSide();
        return Result.success(list);
    }

    /**
     * 申请友链
     * @param linkDTO
     * @return
     */
    @PostMapping("/apply")
    public Result applyLink(@RequestBody LinkDTO linkDTO){
        log.info("申请友链");
        linkDTO.setStatus(2);//默认状态设成审核中
        LinkService.addLink(linkDTO);
        return Result.success();
    }
}
