package top.hazenix.constant;

/**
 * 通用状态常量
 * 用于各种实体的状态字段
 */
public class CommonStatusConstants {
    
    /**
     * 正常/启用状态
     */
    public static final Integer NORMAL = 0;
    
    /**
     * 异常/禁用/锁定状态
     */
    public static final Integer ABNORMAL = 1;
    
    /**
     * 草稿状态（用于文章）
     */
    public static final Integer DRAFT = 2;
    
    /**
     * 待审核状态（用于文章）
     */
    public static final Integer PENDING = 1;
    
    /**
     * 不展示状态（用于评论）
     */
    public static final Integer HIDDEN = 1;
    
    private CommonStatusConstants() {
        // 工具类，禁止实例化
    }
}

