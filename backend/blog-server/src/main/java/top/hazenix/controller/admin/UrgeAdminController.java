package top.hazenix.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.entity.ArticleUrge;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleUrgeService;

import java.util.List;

@RestController
@RequestMapping("/admin/urge")
@RequiredArgsConstructor
public class UrgeAdminController {

    private final ArticleUrgeService urgeService;

    @GetMapping("/stats")
    public Result<List<ArticleUrge>> stats() {
        return Result.success(urgeService.getStats());
    }
}