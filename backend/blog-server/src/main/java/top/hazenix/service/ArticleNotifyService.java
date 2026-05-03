package top.hazenix.service;

import top.hazenix.entity.Article;

public interface ArticleNotifyService {
    void notifySubscribers(Article article);
}
