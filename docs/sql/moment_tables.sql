-- 手记主表
CREATE TABLE moment (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(100),
    content      TEXT NOT NULL,
    images       TEXT,
    like_count   INT NOT NULL DEFAULT 0,
    view_count   INT NOT NULL DEFAULT 0,
    status       SMALLINT NOT NULL DEFAULT 0,
    create_time  TIMESTAMP NOT NULL,
    update_time  TIMESTAMP NOT NULL
);

-- 手记标签表
CREATE TABLE moment_tag (
    moment_id  BIGINT NOT NULL,
    tag_name   VARCHAR(30) NOT NULL,
    PRIMARY KEY (moment_id, tag_name)
);

-- 手记点赞表（IP 幂等）
CREATE TABLE moment_like (
    moment_id   BIGINT NOT NULL,
    ip          VARCHAR(50) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    PRIMARY KEY (moment_id, ip)
);
