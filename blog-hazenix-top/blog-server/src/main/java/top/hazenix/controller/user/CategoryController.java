package top.hazenix.controller.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.CategoryService;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.CategoryVO;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/api/user/categories")
@Slf4j
public class CategoryController {


    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleService articleService;
    /**
     * 获取分类列表
     * @return
     */
    @GetMapping
    public Result getCategoryList(){
        log.info("获取分类列表");
        List<CategoryVO> list =  categoryService.getCategoryList();
        return Result.success(list);
    }

    /**
     * 获取某个分类下的文章列表
     * @param id
     * @return
     */
    @GetMapping("/{id}/articles")
    public Result getCategoryArticles(@PathVariable Integer id){
        log.info("获取分类文章列表:{}",id);
        ArticleListQuery articleListQuery = ArticleListQuery.builder()
                .categoryId(id)
                .status(0)
                .build();
        List<ArticleDetailVO> list = articleService.getArticleList(articleListQuery);
        return Result.success(list);
    }






}
