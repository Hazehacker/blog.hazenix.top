package top.hazenix.controller.user;

import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.entity.Link;
import top.hazenix.result.Result;
import top.hazenix.service.LinkService;

import java.util.List;

@RestController("UserLinkController")
@Slf4j
@RequestMapping("/user/links")
@ApiModel("友链相关接口")
public class LinkController {
    @Autowired
    private LinkService LinkService;

    @GetMapping
    public Result getLinks(){
        log.info("获取友链列表");
        List<Link> list = LinkService.listLinksUserSide();
        return Result.success(list);
    }
}
