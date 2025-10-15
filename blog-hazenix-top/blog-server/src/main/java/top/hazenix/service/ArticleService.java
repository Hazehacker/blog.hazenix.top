package top.hazenix.service;

import top.hazenix.vo.ArticleShortVO;

import java.util.List;

public interface ArticleService {
    List<ArticleShortVO> getRecentArticles(int i);
}
