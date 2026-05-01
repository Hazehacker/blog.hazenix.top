package top.hazenix.constant;

public class RecommendConstants {
    // 行为类型
    public static final int BEHAVIOR_VIEW = 1;
    public static final int BEHAVIOR_LIKE = 2;
    public static final int BEHAVIOR_FAVORITE = 3;

    // 行为评分权重
    public static final int SCORE_VIEW = 1;
    public static final int SCORE_VIEW_LONG = 2; // 阅读 > 30s
    public static final int SCORE_LIKE = 3;
    public static final int SCORE_FAVORITE = 5;
    public static final int LONG_READ_THRESHOLD = 30; // 秒

    // 三路混合权重
    public static final double WEIGHT_CONTENT = 0.4;
    public static final double WEIGHT_CF = 0.4;
    public static final double WEIGHT_POPULARITY = 0.2;

    // 冷启动权重
    public static final double COLD_WEIGHT_CONTENT = 0.6;
    public static final double COLD_WEIGHT_POPULARITY = 0.4;

    // 匿名推荐权重
    public static final double ANON_WEIGHT_POPULARITY = 0.5;
    public static final double ANON_WEIGHT_FRESHNESS = 0.3;
    public static final double ANON_WEIGHT_DIVERSITY = 0.2;

    // 冷启动阈值
    public static final int COLD_START_THRESHOLD = 5;

    // 推荐数量
    public static final int RECOMMEND_SIZE = 10;

    // 同分类加分
    public static final double SAME_CATEGORY_BONUS = 0.2;

    // 时间衰减因子
    public static final double TIME_DECAY_LAMBDA = 0.01;

    // CF 最低行为阈值
    public static final int CF_MIN_BEHAVIOR_COUNT = 5;

    // 兴趣来源
    public static final int INTEREST_SOURCE_REGISTER = 1;
    public static final int INTEREST_SOURCE_BEHAVIOR = 2;

    // 默认兴趣权重
    public static final double DEFAULT_INTEREST_WEIGHT = 0.5;

    // Redis key 前缀
    public static final String KEY_REC_USER = "rec:user:";
    public static final String KEY_REC_ANONYMOUS = "rec:anonymous";
    public static final String KEY_REC_COLD = "rec:cold:";
    public static final String KEY_SIM_CONTENT = "sim:content:";
    public static final String KEY_SIM_CF = "sim:cf:";
    public static final String KEY_BEHAVIOR_SCORE = "behavior:score:";

    // TTL (秒)
    public static final long TTL_REC_USER = 7200;       // 2h
    public static final long TTL_REC_ANONYMOUS = 3600;   // 1h
    public static final long TTL_SIM_CONTENT = 86400;    // 24h
    public static final long TTL_SIM_CF = 43200;         // 12h
    public static final long TTL_BEHAVIOR_SCORE = 604800; // 7d

    // 同步计算超时 (ms)
    public static final long SYNC_COMPUTE_TIMEOUT = 500;

    private RecommendConstants() {}
}
