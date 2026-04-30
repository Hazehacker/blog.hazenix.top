package top.hazenix.constant;

/**
 * 默认值常量
 */
public class DefaultConstants {
    
    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE = 1;
    
    /**
     * 默认页大小（小）
     */
    public static final Integer DEFAULT_PAGE_SIZE_SMALL = 10;
    
    /**
     * 默认页大小（中）
     */
    public static final Integer DEFAULT_PAGE_SIZE_MEDIUM = 20;
    
    /**
     * 默认页大小（大）
     */
    public static final Integer DEFAULT_PAGE_SIZE_LARGE = 30;
    
    /**
     * 获取最新文章数量
     */
    public static final Integer RECENT_ARTICLES_COUNT = 5;
    
    /**
     * 获取最新评论数量
     */
    public static final Integer RECENT_COMMENTS_COUNT = 5;
    
    /**
     * 获取热门文章数量
     */
    public static final Integer POPULAR_ARTICLES_COUNT = 5;

    /**
     * 热门文章 Redis 缓存 key
     */
    public static final String POPULAR_ARTICLES_REDIS_KEY = "popular:articles";

    /**
     * 热门文章 Top N（实际写入 Redis 的条数）
     */
    public static final int POPULAR_ARTICLES_TOP_N = 8;

    /**
     * 热度得分权重：浏览数
     */
    public static final int POPULAR_SCORE_WEIGHT_VIEW = 1;

    /**
     * 热度得分权重：点赞数
     */
    public static final int POPULAR_SCORE_WEIGHT_LIKE = 3;

    /**
     * 热度得分权重：收藏数
     */
    public static final int POPULAR_SCORE_WEIGHT_FAVORITE = 5;

    /**
     * 默认用户ID（系统用户）
     */
    public static final Long DEFAULT_USER_ID = 1L;
    
    /**
     * RequestId 长度
     */
    public static final int REQUEST_ID_LENGTH = 8;
    
    private DefaultConstants() {
        // 工具类，禁止实例化
    }
}

