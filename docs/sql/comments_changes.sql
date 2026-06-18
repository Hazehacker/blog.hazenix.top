ALTER TABLE comments
  ADD COLUMN email VARCHAR(100) NULL COMMENT '匿名评论者邮箱，可空' AFTER reply_username;