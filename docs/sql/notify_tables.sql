CREATE TABLE IF NOT EXISTS notify_config (
  id            BIGINT       PRIMARY KEY,
  recipient     VARCHAR(255) NOT NULL  COMMENT '收件人邮箱',
  smtp_host     VARCHAR(255) NOT NULL,
  smtp_port     INT          NOT NULL,
  smtp_username VARCHAR(255) NOT NULL,
  smtp_password VARCHAR(512) NOT NULL  COMMENT 'AES 加密后存储',
  smtp_ssl      TINYINT(1)   NOT NULL DEFAULT 1,
  send_time     VARCHAR(8)   NOT NULL DEFAULT '08:00' COMMENT 'HH:mm',
  enabled       TINYINT(1)   NOT NULL DEFAULT 1,
  update_time   DATETIME
);

INSERT IGNORE INTO notify_config (id, recipient, smtp_host, smtp_port, smtp_username, smtp_password, send_time, enabled)
VALUES (1, 'admin@example.com', 'smtp.qq.com', 465, 'your@qq.com', '', '08:00', 0);

CREATE TABLE IF NOT EXISTS notify_log (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  stat_date    DATE         NOT NULL  COMMENT '统计目标日（昨日）',
  send_time    DATETIME     NOT NULL  COMMENT '本次实际发送时间',
  status       TINYINT      NOT NULL  COMMENT '0 成功 / 1 失败',
  retry_count  INT          NOT NULL DEFAULT 0,
  error_msg    TEXT,
  recipient    VARCHAR(255),
  KEY idx_stat_date (stat_date)
);

CREATE TABLE IF NOT EXISTS notify_action_token (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  token       VARCHAR(64)  NOT NULL UNIQUE COMMENT '随机 token (URL-safe)',
  target_type VARCHAR(32)  NOT NULL COMMENT 'link',
  target_id   BIGINT       NOT NULL,
  action      VARCHAR(16)  NOT NULL COMMENT 'approve / reject',
  expires_at  DATETIME     NOT NULL,
  used_at     DATETIME,
  create_time DATETIME     NOT NULL,
  KEY idx_token (token),
  KEY idx_expires (expires_at)
);
