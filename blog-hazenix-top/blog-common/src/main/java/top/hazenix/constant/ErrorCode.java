package top.hazenix.constant;

/**
 * 错误码常量类
 * 错误码格式：A-BB-CCC
 * A: 错误来源 (1位) - A:用户错误, B:系统错误, C:第三方错误
 * BB: 业务域 (2位) - 00:通用, 01:用户中心, 02:文章中心, 03:评论中心, 04:分类中心, 05:标签中心, 06:友链中心, 07:树洞评论中心
 * CCC: 具体错误编号 (3位)
 */
public class ErrorCode {

    // ==================== 00 - 通用/基础架构 ====================
    /** 参数验证失败 */
    public static final String A00001 = "A00001";
    /** 未授权 */
    public static final String A00002 = "A00002";
    /** 系统内部错误 */
    public static final String B00001 = "B00001";
    /** 数据库连接失败 */
    public static final String B00002 = "B00002";
    /** 配置加载失败 */
    public static final String B00003 = "B00003";
    /** 文件上传失败 */
    public static final String B00004 = "B00004";
    /** 第三方服务调用失败 */
    public static final String C00001 = "C00001";
    /** 第三方服务超时 */
    public static final String C00002 = "C00002";

    // ==================== 01 - 用户中心 ====================
    /** 当前邮箱还未注册 */
    public static final String A01001 = "A01001";
    /** 邮箱或密码错误 */
    public static final String A01002 = "A01002";
    /** 用户未登录 */
    public static final String A01003 = "A01003";
    /** 当前邮箱已经注册过账号 */
    public static final String A01004 = "A01004";
    /** 当前用户已被拉入黑名单 */
    public static final String A01005 = "A01005";
    /** id_token不能为空 */
    public static final String A01006 = "A01006";
    /** 无效的audience */
    public static final String A01007 = "A01007";
    /** 邮箱格式不正确 */
    public static final String A01008 = "A01008";
    /** 用户昵称不超过30个字符 */
    public static final String A01009 = "A01009";
    /** 当前密码填写错误 */
    public static final String A01010 = "A01010";
    /** 新密码不能和当前密码相同 */
    public static final String A01011 = "A01011";
    /** ID Token 验证失败 */
    public static final String B01001 = "B01001";
    /** 无效的issuer */
    public static final String B01002 = "B01002";
    /** 从github获取access token失败 */
    public static final String C01001 = "C01001";

    // ==================== 02 - 文章中心 ====================
    /** 不存在该文章 */
    public static final String A02001 = "A02001";
    /** 文章内容不能为空 */
    public static final String A02002 = "A02002";
    /** 文章字数超出限制 */
    public static final String A02003 = "A02003";
    /** 登录之后才能收藏文章 */
    public static final String A02004 = "A02004";
    /** 要删除的文章不能为空 */
    public static final String A02005 = "A02005";

    // ==================== 03 - 评论中心 ====================
    /** 不存在该被回复者 */
    public static final String A03001 = "A03001";

    // ==================== 04 - 分类中心 ====================
    /** 当前分类关联了文章，不能删除 */
    public static final String A04001 = "A04001";

    // ==================== 05 - 标签中心 ====================
    /** 当前标签关联了文章，不能删除 */
    public static final String A05001 = "A05001";


}

