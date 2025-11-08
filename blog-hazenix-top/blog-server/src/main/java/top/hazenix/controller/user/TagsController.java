package top.hazenix.controller.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.TagsService;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.TagsVO;

import java.util.List;

@RestController("UserTagsController")
@RequestMapping("/user/tags")
@Slf4j
@RequiredArgsConstructor
public class TagsController {

    private final TagsService tagsService;

    private final ArticleService articleService;

    /**
     * 获取标签列表
     * @return
     */
    @GetMapping
    @Cacheable(cacheNames = "tagsCache",key = "#root.method")
    public Result getTagsList(){
        log.info("获取标签列表");
        List<TagsVO> list = tagsService.getTagsList();
        return Result.success(list);
    }


    /**
     * 获取某个标签下的文章列表
     * @param id
     * @return
     */
    @GetMapping("/{id}/articles")
    @Cacheable(cacheNames = "tagsCache",key = "#id")
    public Result getTagsArticles(@PathVariable Long id){
        log.info("获取某个标签下的文章列表:{}",id);
        ArticleListQuery articleListQuery = ArticleListQuery.builder()
                .tagId(id)
                .status(0)
                .build();
        List<ArticleDetailVO> list = articleService.getArticleList(articleListQuery);
        return Result.success(list);
    }


}
