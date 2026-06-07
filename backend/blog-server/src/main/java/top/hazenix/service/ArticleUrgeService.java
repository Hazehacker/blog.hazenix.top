package top.hazenix.service;

import top.hazenix.entity.ArticleUrge;
import java.util.List;

public interface ArticleUrgeService {
    int urgeAndGetCount();
    int getCurrentMonthCount();
    List<ArticleUrge> getStats();
}
