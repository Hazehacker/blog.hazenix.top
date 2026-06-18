package top.hazenix.service;

import top.hazenix.dto.ArticleDTO;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.PageResult;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;
import top.hazenix.vo.ArticleSlugVO;

import java.util.List;

public interface ArticleService {
    List<ArticleShortVO> getRecentArticles(int i);

    PageResult pageQuery(Integer page, Integer pageSize, String keyword, Integer categoryId, Integer status);

    ArticleDetailVO getArticleDetail(Long id);

    void addArticle(ArticleDTO articleDTO);

    void updateArticle(Long id, ArticleDTO articleDTO);

    void deleteArticles(List<Long> ids);

    void deleteArticle(Long id);

    void updateArticleStatus(Long id, Integer status);

    void updateRecommendLevel(Long id, Integer level);

    List<ArticleDetailVO> getArticleList(ArticleListQuery articleListQuery);

    /** 轻量列表查询（不含正文），用于前端首页/列表页 */
    List<ArticleShortVO> getArticleShortList(ArticleListQuery articleListQuery);

    ArticleDetailVO getArticleDetailBySlug(String slug);

    List<ArticleShortVO> getPopularArticles(int i);

    void updateArticleView(Long id);

    void likeArticle(Long id);

    void favoriteArticle(Long id);

    List<ArticleShortVO> getRelatedArticles(Long id,Integer limit);

    void addArticleViewByMe(Long id, Integer count);

    /**
     * 获取所有已发布文章的slug列表（用于SEO Sitemap）
     */
    List<ArticleSlugVO> getPublishedArticleSlugs();
}
