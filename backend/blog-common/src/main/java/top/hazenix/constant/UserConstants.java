package top.hazenix.constant;

/**
 * 用户相关常量
 */
public class UserConstants {
    
    /**
     * 用户角色：管理员
     */
    public static final Integer ROLE_ADMIN = 0;
    
    /**
     * 用户角色：作者
     */
    public static final Integer ROLE_AUTHOR = 1;
    
    /**
     * 用户角色：普通用户（默认）
     */
    public static final Integer ROLE_USER = 2;
    
    /**
     * 用户状态：正常
     */
    public static final Integer STATUS_NORMAL = 0;
    
    /**
     * 用户状态：锁定
     */
    public static final Integer STATUS_LOCKED = 1;
    
    /**
     * 性别：女
     */
    public static final Integer GENDER_FEMALE = 0;
    
    /**
     * 性别：男
     */
    public static final Integer GENDER_MALE = 1;
    
    /**
     * 用户名最大长度
     */
    public static final int USERNAME_MAX_LENGTH = 30;
    
    /**
     * 默认用户ID（系统用户）
     */
    public static final Long DEFAULT_USER_ID = 1L;
    
    private UserConstants() {
        // 工具类，禁止实例化
    }
}

