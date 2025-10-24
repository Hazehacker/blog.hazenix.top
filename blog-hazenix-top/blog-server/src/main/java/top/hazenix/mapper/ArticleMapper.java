package top.hazenix.mapper;


import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import springfox.documentation.oas.mappers.LicenseMapper;
import top.hazenix.annotation.AutoFill;
import top.hazenix.entity.Article;
import top.hazenix.enumeration.OperationType;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.vo.ArticleShortVO;

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
}
