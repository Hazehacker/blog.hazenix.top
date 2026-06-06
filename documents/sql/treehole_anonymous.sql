-- 树洞匿名功能：新增邮箱、匿名标识、IP字段
ALTER TABLE tree_comments
    ADD COLUMN email VARCHAR(100) NULL COMMENT '匿名用户邮箱',
    ADD COLUMN is_anonymous TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名 0=否 1=是',
    ADD COLUMN ip VARCHAR(45) NULL COMMENT '客户端IP';
