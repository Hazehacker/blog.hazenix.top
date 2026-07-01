package top.hazenix.controller.admin;


import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.MessageConstant;
import top.hazenix.constant.DefaultConstants;
import top.hazenix.dto.ArticleDTO;
import top.hazenix.dto.DeleteArticleRequestDTO;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.DeepSeekService;
import top.hazenix.service.PrerenderNotifyService;
import top.hazenix.service.impl.ArticleServiceImpl;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController("AdminArticleController")
@Slf4j
@RequestMapping("/admin/articles")
@ApiModel("文章相关接口")
@RequiredArgsConstructor
public class ArticleController {



    private final ArticleService articleService;
    private final PrerenderNotifyService prerenderNotifyService;
    private final DeepSeekService deepSeekService;


    /**
     * 获取最新的文章列表（5篇）
     * @return
     */
    @GetMapping("/recent")
    @Cacheable(cacheNames = "articlesCache", key = "#root.method")
    public Result getRecentArticles() {
        List<ArticleShortVO> list = articleService.getRecentArticles(DefaultConstants.RECENT_ARTICLES_COUNT);
        return Result.success(list);
    }


    /**
     * 获取文章列表（分页展示支持筛选）
     * @return
     * [用得少，暂时不加redis]
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
    @Cacheable(cacheNames = "articlesCache", key = "#id")
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
    @CacheEvict(cacheNames = "articlesCache", allEntries = true)
    public Result addArticle(@Valid @RequestBody ArticleDTO articleDTO){
        log.info("新增文章：{}",articleDTO);
        articleService.addArticle(articleDTO);
        prerenderNotifyService.notifyContentChanged("article", null);
        return Result.success();
    }

    /**
     * 更新文章
     * @param id
     * @param articleDTO
     * @return
     */
    @PutMapping("/{id}")
    @CacheEvict(cacheNames = "articlesCache", key = "#id")
    public Result updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDTO){
        log.info("更新文章：{}",id);
        articleService.updateArticle(id,articleDTO);
        prerenderNotifyService.notifyContentChanged("article", id);
        return Result.success();
    }

    /**
     * 删除单个文章
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "articlesCache", allEntries = true)
    public Result deleteArticle(@PathVariable Long id){
        log.info("删除文章：{}",id);
        articleService.deleteArticle(id);
        prerenderNotifyService.notifyContentChanged("article", id);
        return Result.success();
    }

    /**
     * 批量删除文章
     * @param
     * @return
     */
    @DeleteMapping("/batch")
    @CacheEvict(cacheNames = "articlesCache", allEntries = true)
    public Result deleteArticles(@Valid @RequestBody DeleteArticleRequestDTO deleteArticleRequestDTO){
        List<Long> ids = deleteArticleRequestDTO.getIds();
        log.info("批量删除文章：{}",ids);
        if(ids.isEmpty()){
            return Result.error(ErrorCode.A02005, MessageConstant.ARTICLLE_TO_BE_DELETED_IS_NULL);
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
    @CacheEvict(cacheNames = "articlesCache", allEntries = true)
    public Result updateArticleStatus(@PathVariable Long id,@PathVariable Integer status){
        log.info("更新文章状态：{}",id);
        articleService.updateArticleStatus(id,status);
        prerenderNotifyService.notifyContentChanged("article", id);
        return Result.success();
    }

    /**
     * 更新文章推荐度
     * @param id 文章ID
     * @param level 推荐度 0-5
     * @return
     */
    @PutMapping("/{id}/recommend-level")
    public Result updateRecommendLevel(
            @PathVariable Long id,
            @RequestParam @Min(0) @Max(5) Integer level) {
        log.info("管理员修改文章 {} 推荐度为 {}", id, level);
        articleService.updateRecommendLevel(id, level);
        return Result.success();
    }

    /**
     * AI 生成文章 slug（DeepSeek）
     */
    @PostMapping("/slug/generate")
    public Result generateSlug(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return Result.error(top.hazenix.constant.ErrorCode.A02002, "标题不能为空");
        }
        String slug = deepSeekService.generateSlug(title);
        if (slug == null) {
            return Result.error(top.hazenix.constant.ErrorCode.A04001, "AI 生成失败，请稍后重试");
        }
        return Result.success(slug);
    }


}
