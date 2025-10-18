package top.hazenix.service;

import top.hazenix.dto.ArticleDTO;
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
}
