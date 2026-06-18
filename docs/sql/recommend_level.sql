-- 添加推荐度字段
ALTER TABLE article
ADD COLUMN recommend_level TINYINT NOT NULL DEFAULT 3
COMMENT '推荐度 0=屏蔽 1-5=推荐等级(3为默认)';

-- 创建索引用于过滤屏蔽文章
CREATE INDEX idx_recommend_level ON article(recommend_level);