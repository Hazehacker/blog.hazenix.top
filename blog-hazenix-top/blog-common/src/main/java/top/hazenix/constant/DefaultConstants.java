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

