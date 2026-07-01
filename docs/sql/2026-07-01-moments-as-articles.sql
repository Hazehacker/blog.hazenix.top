-- 2026-07-01 手记并入文章模型
-- 1. 分类加类型列（0=普通，1=手记），并种子手记分类
ALTER TABLE category ADD COLUMN IF NOT EXISTS type SMALLINT NOT NULL DEFAULT 0;

INSERT INTO category (name, type, status, sort, slug, create_time, update_time)
SELECT '手记', 1, 0, 999, 'moments', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE type = 1);

-- 2. 文章加图片列（仅手记填充，JSON 数组字符串）
ALTER TABLE article ADD COLUMN IF NOT EXISTS images TEXT;

-- 3. moment_like 列重命名：moment_id -> article_id（引用 article.id）
ALTER TABLE moment_like RENAME COLUMN moment_id TO article_id;

-- 4. 废弃旧表（确认无数据后）
DROP TABLE IF EXISTS moment_tag;
DROP TABLE IF EXISTS moment;
