package top.hazenix.controller.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

import java.util.List;

@RestController("UserArticleController")
@RequestMapping("/user/articles")
@Slf4j
@RequiredArgsConstructor
public class ArticleController {


    private final ArticleService articleService;

    /**
     * 获取文章列表（用于用户端）
     * @param articleListQuery
     * @return
     */
    @GetMapping
    @Cacheable(cacheNames = "articlesCache", key = "#root.args[0].title+ '_' + #root.args[0].categoryId+ '_' + #root.args[0].tagId+ '_' + #root.args[0].status+ '_' + #root.args[0].userId")
    public Result getArticleList(ArticleListQuery articleListQuery){
        log.info("获取文章列表");
        //由于用于用户端，只返回不是草稿的文章
        articleListQuery.setStatus(0);
        List<ArticleDetailVO> list = articleService.getArticleList(articleListQuery);
        return Result.success(list);
    }

    @GetMapping("/{id}/related")
    @Cacheable(cacheNames = "articlesCache", key = "#id+ '_' +#limit")
    public Result getRelatedArticles(@PathVariable Long id, Integer limit){
        log.info("获取相关文章");
        List<ArticleShortVO> list = articleService.getRelatedArticles(id,limit);
        return Result.success(list);
    }

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Cacheable(cacheNames = "articlesCache", key = "#id")
    public Result getArticleDetail(@PathVariable Long id){
        log.info("获取文章详情");
        ArticleDetailVO articleDetailVO = articleService.getArticleDetail(id);
        return Result.success(articleDetailVO);
    }

    /**
     * 用slug获取文章详情（不知道写来干嘛，但是前端生成的接口文档里有，先写了）
     * @param slug
     * @return
     */
    @GetMapping("/slug/{slug}")
    public Result getArticleDetail(@PathVariable String slug){
        log.info("获取文章详情");
        ArticleDetailVO articleDetailVO = articleService.getArticleDetailBySlug(slug);
        return Result.success(articleDetailVO);
    }
    /**
     * 获取热门文章
     * @return
     */
    @GetMapping("/popular")
    public Result getPopularArticles(){
        log.info("获取最热文章");
        //返回值用shortVO即可
        List<ArticleShortVO> list = articleService.getPopularArticles(8);
        return Result.success(list);
    }
    /**
     * 获取推荐文章
     * @return
     */
    @GetMapping("/recommended")
    public Result getRecommendedArticles(){
        log.info("获取推荐文章");
        //TODO 后期做个性化推荐的时候再实现
//        List<ArticleDetailVO> list = articleService.getRecommendedArticles(5);
//        return Result.success(list);
        return Result.success();
    }
    //### 2.10 获取推荐文章
    //- **URL**: `GET /user/articles/recommended`
    //- **描述**: 获取推荐文章列表
    //- **认证**: 需要（可选）
    //- **请求参数**:
    //  - `limit` (int, 可选): 限制数量，默认10
    //- **响应示例**: ArticleShortVO列表






    /**
     * 获取已发布文章的slug列表（用于SEO）
     * @return
     */
    @GetMapping("/slugs")
    public Result getArticlesSlugList(){
        log.info("获取文章slug列表");
        //TODO 后期做
        return null;
    }
    //### 2.13 获取所有已发布文章的slug
    //- **URL**: `GET /user/articles/slugs`
    //- **描述**: 获取所有已发布文章的slug列表（用于SEO）
    //- **认证**: 不需要
    //- **响应示例**:
    //```json
    //{
    //  "code": 200,
    //  "msg": "success",
    //  "data": [
    //    {
    //      "id": 1,
    //      "slug": "article-slug-1",
    //      "updateTime": "2024-01-01T00:00:00Z"
    //    },
    //    {
    //      "id": 2,
    //      "slug": "article-slug-2",
    //      "updateTime": "2024-01-02T00:00:00Z"
    //    }
    //  ]
    //}
    //```




    /**
     * 用户点赞文章/取消点赞文章
     * @param id
     * @return
     */
    @PostMapping("/{id}/like")
    public Result likeArticle(@PathVariable Long id){
        log.info("点赞/取消点赞文章:{}",id);
        //添加isLiked属性，建一个user_article表(user_id,article_id,is_liked,is_favorite)

        //用户点赞或者收藏之后再往user_article表插入数据，不然每次添加文章和新增用户都会插入一堆条目（浪费空间，很多用不到）
        articleService.likeArticle(id);
        return Result.success();
    }

    /**
     * 用户收藏/取消收藏文章
     * @param id
     * @return
     */
    @PostMapping("/{id}/favorite")
    public Result favoriteArticle(@PathVariable Long id){
        log.info("收藏文章:{}",id);
        //添加isFavorite属性，建一个user_article表(user_id,article_id,is_liked,is_favorite)

        //修改user_article关联表中的is_favorite字段
        //增加或减少文章的收藏数
        articleService.favoriteArticle(id);

        return Result.success();
    }

    /**
     * 用户浏览文章
     * @param id
     * @return
     */
    @PutMapping("/{id}/view")
    public Result updateArticleView(@PathVariable Long id){
        //TODO 后期可以做一个redis缓存，然后定期持久化到数据库
        log.info("更新文章浏览数:{}",id);
        articleService.updateArticleView(id);
        return Result.success();
    }

    /**
     * 增加文章浏览量
     * @param id
     * @param count
     * @return
     */
    @PostMapping("/{id}/view/{count}")
    public Result updateArticleView(@PathVariable Long id,@PathVariable Integer count){
        log.info("更新文章浏览数:{}",id);
        articleService.addArticleViewByMe(id,count);
        return Result.success();
    }




}
