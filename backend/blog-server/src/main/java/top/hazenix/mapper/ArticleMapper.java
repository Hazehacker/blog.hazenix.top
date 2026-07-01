package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import springfox.documentation.oas.mappers.LicenseMapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.entity.Article;
import top.hazenix.enumeration.OperationType;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.vo.ArticleShortVO;
import top.hazenix.vo.ArticleSlugVO;
import top.hazenix.vo.MomentVO;

import java.util.List;

@Mapper
public interface ArticleMapper {

    /**
     * 分页查询文章列表（可以根据关键词、分类、文章状态过滤）
     * @param keyword
     * @param categoryId
     * @param status
     * @return
     */
    Page<ArticleShortVO> pageQuery(String keyword,Integer categoryId,Integer status);

    /**
     * 统计文章数量
     * @return
     */
    Integer count();

    /**
     * 统计某一分类id集合下的文章数量
     * @param ids
     * @return
     */
    Integer countByIds(List<Integer> ids);
    /**
     * 根据标签获取相关文章
     * 【返回与当前文章有共同标签的文章，按照共同标签数排序】
     * @param articleId 当前文章ID
     * @param tagIds 标签ID列表
     * @param limit 限制数量
     * @return 相关文章列表
     */
    List<ArticleShortVO> getRelatedArticlesByTags(Long articleId,
                                                  List<Integer> tagIds,
                                                  Integer limit);

    /**
     * 根据分类获取相关文章
     * @param articleId 当前文章ID
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 相关文章列表
     */
    List<ArticleShortVO> getRelatedArticlesByCategory(Long articleId,
                                                      Integer categoryId,
                                                      Integer limit);



    /**
     * 获取最新的文章列表
     * @param i
     * @return
     */
    List<ArticleShortVO> getRecentArticles(int i);

    /**
     * 根据id获取文章详情（返回article对象）
     * @param id
     * @return
     */
    Article getById(Long id);

    /**
     * 新增文章
     * @param article
     */
    @AutoFill(OperationType.INSERT)
    void insert(Article article);

    /**
     * 更新文章
     * @param article
     */
    @AutoFill(OperationType.UPDATE)
    void update(Article article);

    /**
     * 批量删除文章
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新文章推荐等级
     * @param id 文章ID
     * @param level 推荐等级
     */
    void updateRecommendLevel(@Param("id") Long id, @Param("level") Integer level);

    /**
     * 获取文章列表（用于用户端）
     * @param articleListQuery
     * @return
     */
    List<Article> getArticleList(ArticleListQuery articleListQuery);

    /**
     * 根据slug获取文章(返回article对象)
     * @param slug
     * @return
     */
    Article getBySlug(String slug);

    /**
     * 获取热门文章(点赞量+阅读量前i篇的文章)
     * @param i
     * @return
     */
    List<Article> getPopularArticles(int i);

    /**
     * 获取所有正常状态(status=0)文章，仅含算分需要的字段
     * @return 文章列表
     */
    List<Article> listAllForScoring();

    /**
     * 获取所有已发布文章的slug列表（用于SEO Sitemap）
     * @return 文章slug列表
     */
    List<ArticleSlugVO> getPublishedSlugs();

    /**
     * 手记匿名点赞：直接累加 like_count（无需登录）
     * @param id 文章ID
     */
    void incrementLikeCount(Long id);

    /**
     * 手记浏览数 +1
     * @param id 文章ID
     */
    void incrementViewCount(Long id);

    /**
     * 分页查询手记列表（手记存储在文章表的手记分类下）
     * @param keyword 关键词（标题或内容模糊匹配）
     * @param status 状态过滤
     * @param momentCategoryId 手记分类ID
     * @return 手记VO分页结果
     */
    com.github.pagehelper.Page<top.hazenix.vo.MomentVO> pageMoments(
            @org.apache.ibatis.annotations.Param("keyword") String keyword,
            @org.apache.ibatis.annotations.Param("status") Integer status,
            @org.apache.ibatis.annotations.Param("momentCategoryId") Integer momentCategoryId);
}
