package top.hazenix.controller.admin;


import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.ArticleDTO;
import top.hazenix.dto.DeleteArticleRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.impl.ArticleServiceImpl;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/articles")
@ApiModel("文章相关接口")
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


    /**
     * 获取文章列表（支持筛选）
     * @return
     */
    //categoryID可以为空
    @GetMapping
    public Result<PageResult> getArticles(
            @RequestParam(defaultValue = "1")Integer page,
            @RequestParam(defaultValue = "20")Integer pageSize,
            String keyword,
            Integer categoryId,
            Integer status
    ){

        PageResult pageResult = articleService.pageQuery(page,pageSize,keyword,categoryId,status);

        return Result.success(pageResult);
    }



    /**
     * 获取文章详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getArticleDetail(@PathVariable Long id){
        log.info("获取文章详情:{}",id);
        ArticleDetailVO articleDetailVO = articleService.getArticleDetail(id);
        return Result.success(articleDetailVO);
    }

    /**
     * 新增文章
     * @param articleDTO
     * @return
     */
    @PostMapping
    public Result addArticle(@RequestBody ArticleDTO articleDTO){
        log.info("新增文章：{}",articleDTO);
        articleService.addArticle(articleDTO);
        return Result.success();
    }

    /**
     * 更新文章
     * @param id
     * @param articleDTO
     * @return
     */
    @PutMapping("/{id}")
    public Result updateArticle(@PathVariable Long id,@RequestBody ArticleDTO articleDTO){
        log.info("更新文章：{}",id);
        articleService.updateArticle(id,articleDTO);
        return Result.success();
    }

    /**
     * 删除单个文章
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteArticle(@PathVariable Long id){
        log.info("删除文章：{}",id);
        articleService.deleteArticle(id);
        return Result.success();
    }

    /**
     * 批量删除文章
     * @param ids
     * @return
     */
    @DeleteMapping("/batch")
    public Result deleteArticles(@RequestBody DeleteArticleRequestDTO deleteArticleRequestDTO){
        List<Long> ids = deleteArticleRequestDTO.getIds();
        log.info("批量删除文章：{}",ids);
        if(ids.size()==1){
            return null;
        }
        articleService.deleteArticles(ids);
        return Result.success();
    }

    /**
     * 更新文章状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/{id}/{status}")
    public Result updateArticleStatus(@PathVariable Long id,@PathVariable Integer status){
        log.info("更新文章状态：{}",id);
        articleService.updateArticleStatus(id,status);
        return Result.success();
    }


}
