CREATE TABLE IF NOT EXISTS article_subscription (
  id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
  email             VARCHAR(255) NOT NULL UNIQUE,
  unsubscribe_token VARCHAR(64)  NOT NULL UNIQUE,
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '1=已激活 2=已退订',
  subscribe_at      DATETIME     NOT NULL,
  unsubscribe_at    DATETIME,
  KEY idx_email (email),
  KEY idx_unsubscribe_token (unsubscribe_token)
);

CREATE TABLE IF NOT EXISTS article_urge (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  urge_month VARCHAR(7)  NOT NULL COMMENT 'YYYY-MM',
  count      INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_month (urge_month)
);

INSERT IGNORE INTO article_urge (urge_month, count)
VALUES (DATE_FORMAT(CURDATE(), '%Y-%m'), 0);

CREATE TABLE IF NOT EXISTS site_like (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  ip_hash    VARCHAR(64)  NOT NULL COMMENT 'IP 哈希值，防刷量',
  created_at DATETIME     NOT NULL,
  UNIQUE KEY uk_ip (ip_hash)
);

CREATE TABLE IF NOT EXISTS article_notify_log (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id    BIGINT       NOT NULL,
  send_time     DATETIME     NOT NULL,
  success_count INT          NOT NULL DEFAULT 0,
  fail_count    INT          NOT NULL DEFAULT 0,
  KEY idx_article (article_id)
);