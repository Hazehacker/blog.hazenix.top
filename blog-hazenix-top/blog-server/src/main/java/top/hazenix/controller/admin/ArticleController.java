package top.hazenix.controller.admin;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.impl.ArticleServiceImpl;
import top.hazenix.vo.ArticleShortVO;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    /**
     * 获取最新点的文章列表（5篇）
     * @return
     */
    @GetMapping("/recent")
    public Result getRecentArticles() {
        List<ArticleShortVO> list = articleService.getRecentArticles(5);
        return Result.success(list);
    }


}
