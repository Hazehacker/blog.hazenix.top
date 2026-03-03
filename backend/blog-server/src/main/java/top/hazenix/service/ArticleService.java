package top.hazenix.service;

import top.hazenix.dto.ArticleDTO;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.PageResult;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.ArticleShortVO;

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

    List<ArticleDetailVO> getArticleList(ArticleListQuery articleListQuery);

    ArticleDetailVO getArticleDetailBySlug(String slug);

    List<ArticleShortVO> getPopularArticles(int i);

    void updateArticleView(Long id);

    void likeArticle(Long id);

    void favoriteArticle(Long id);

    List<ArticleShortVO> getRelatedArticles(Long id,Integer limit);

    void addArticleViewByMe(Long id, Integer count);
}
