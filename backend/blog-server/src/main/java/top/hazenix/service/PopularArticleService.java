package top.hazenix.service;

import top.hazenix.vo.ArticleShortVO;

import java.util.List;

public interface PopularArticleService {

    /**
     * 重新计算热门文章并写入 Redis（覆盖式）。
     * 由定时任务 / 启动预热 / cache miss 触发。
     * 异常自行吞掉并 log，不抛出，避免拖垮调度线程。
     */
    void recompute();

    /**
     * 获取热门文章。优先读 Redis；miss 时降级查 DB 并异步触发 recompute。
     * @param limit 返回条数上限
     */
    List<ArticleShortVO> getCachedOrFallback(int limit);
}
