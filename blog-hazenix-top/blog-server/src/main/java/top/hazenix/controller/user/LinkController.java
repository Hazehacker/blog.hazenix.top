package top.hazenix.controller.user;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.LinkDTO;

import javax.validation.Valid;
import top.hazenix.entity.Link;
import top.hazenix.result.Result;
import top.hazenix.service.LinkService;

import java.util.List;

@RestController("UserLinkController")
@Slf4j
@RequestMapping("/user/links")
@ApiModel("友链相关接口")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService LinkService;

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
    public Result applyLink(@Valid @RequestBody LinkDTO linkDTO){
        log.info("申请友链");
        LinkService.addLink(linkDTO);
        return Result.success();
    }
}
