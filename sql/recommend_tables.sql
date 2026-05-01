-- sql/recommend_tables.sql

CREATE TABLE IF NOT EXISTS user_behavior (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    article_id    BIGINT       NOT NULL,
    behavior_type TINYINT      NOT NULL COMMENT '1=浏览 2=点赞 3=收藏',
    duration      INT          DEFAULT NULL COMMENT '阅读时长(秒)，仅浏览行为',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_behavior (user_id, behavior_type),
    INDEX idx_article_behavior (article_id, behavior_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_interest (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    tag_id      BIGINT   NOT NULL,
    weight      DOUBLE   NOT NULL DEFAULT 0.5 COMMENT '兴趣权重 0-1',
    source      TINYINT  NOT NULL COMMENT '1=注册选择 2=行为推断',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_tag (user_id, tag_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
