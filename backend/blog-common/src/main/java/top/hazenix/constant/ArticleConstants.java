package top.hazenix.constant;

/**
 * 文章相关常量
 */
public class ArticleConstants {
    
    /**
     * 文章状态：正常
     */
    public static final Integer STATUS_NORMAL = 0;
    
    /**
     * 文章状态：待审核
     */
    public static final Integer STATUS_PENDING = 1;
    
    /**
     * 文章状态：草稿
     */
    public static final Integer STATUS_DRAFT = 2;
    
    /**
     * 是否置顶：不置顶
     */
    public static final Integer IS_TOP_NO = 0;
    
    /**
     * 是否置顶：置顶
     */
    public static final Integer IS_TOP_YES = 1;
    
    /**
     * 初始点赞数
     */
    public static final Integer INITIAL_LIKE_COUNT = 0;
    
    /**
     * 初始收藏数
     */
    public static final Integer INITIAL_FAVORITE_COUNT = 0;
    
    /**
     * 初始浏览量
     */
    public static final Integer INITIAL_VIEW_COUNT = 1;
    
    /**
     * 文章内容最大字节数（MEDIUMTEXT类型，留点余量）
     */
    public static final int MAX_CONTENT_SIZE_BYTES = 16777220;
    
    /**
     * 文章标题最大长度
     */
    public static final int TITLE_MAX_LENGTH = 200;
    
    /**
     * URL标识符最大长度
     */
    public static final int SLUG_MAX_LENGTH = 100;
    
    private ArticleConstants() {
        // 工具类，禁止实例化
    }
}

