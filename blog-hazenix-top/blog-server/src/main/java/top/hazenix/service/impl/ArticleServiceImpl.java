package top.hazenix.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.service.ArticleService;
import top.hazenix.vo.ArticleShortVO;

import java.util.Collections;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 获取最新点的文章列表
     * @param i
     * @return
     */
    @Override
    public List<ArticleShortVO> getRecentArticles(int i) {
        List<ArticleShortVO> list = articleMapper.getRecentArticles(i);
        return list;
    }
}
