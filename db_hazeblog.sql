/*
 Navicat Premium Dump SQL

 Source Server         : connectWithMySql
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : db_hazeblog

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 03/11/2025 22:20:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `cover_image` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category_id` int NULL DEFAULT NULL,
  `like_count` int NULL DEFAULT NULL,
  `favorite_count` int NULL DEFAULT NULL,
  `view_count` int NULL DEFAULT NULL,
  `slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_top` tinyint(1) NULL DEFAULT NULL COMMENT '用来置顶文章 0不置顶，1置顶',
  `status` tinyint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (1, 1, 'HelloWorld', 'Hello world', NULL, 1, 5, 0, 9, NULL, NULL, 0, '2025-10-15 14:46:24', '2025-11-01 20:06:46');

-- ----------------------------
-- Table structure for article_tags
-- ----------------------------
DROP TABLE IF EXISTS `article_tags`;
CREATE TABLE `article_tags`  (
  `tags_id` int NULL DEFAULT NULL,
  `tags_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `article_id` bigint NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_tags
-- ----------------------------

INSERT INTO `article_tags` VALUES (40, '测试标签', 1);
INSERT INTO `article_tags` VALUES (2, 'Java', 1);
INSERT INTO `article_tags` VALUES (1, 'SpringBoot', 1);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL COMMENT '排序字段',
  `slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '数据库与存储', 0, 4, NULL, '2025-10-15 14:49:01', '2025-10-22 22:46:46');
INSERT INTO `category` VALUES (2, '系统设计与架构', 0, 5, NULL, '2025-10-15 14:49:44', '2025-10-22 22:56:12');
INSERT INTO `category` VALUES (3, '中间件', 0, 0, NULL, '2025-10-15 14:50:00', '2025-10-22 22:46:47');
INSERT INTO `category` VALUES (4, 'DevOps与部署', 0, NULL, NULL, '2025-10-15 14:50:29', '2025-10-22 21:11:37');
INSERT INTO `category` VALUES (5, '面试', 0, NULL, NULL, '2025-10-15 14:51:00', '2025-10-15 14:51:02');
INSERT INTO `category` VALUES (6, '职业发展', 0, NULL, NULL, '2025-10-15 14:52:55', '2025-10-15 14:52:56');
INSERT INTO `category` VALUES (7, '开源项目', 0, NULL, NULL, '2025-10-15 14:53:25', '2025-10-22 21:10:22');
INSERT INTO `category` VALUES (8, '工具与效率', 0, NULL, NULL, '2025-10-15 14:54:01', '2025-10-22 21:10:31');
INSERT INTO `category` VALUES (9, '读书笔记', 0, 1, NULL, '2025-10-15 14:54:05', '2025-10-22 22:53:48');
INSERT INTO `category` VALUES (20, '测试分类', 0, 3, 'test-category', '2025-10-23 20:34:35', '2025-10-23 20:34:35');

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NULL DEFAULT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reply_id` bigint NULL DEFAULT NULL,
  `reply_username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for link
-- ----------------------------
DROP TABLE IF EXISTS `link`;
CREATE TABLE `link`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网站描述',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;



-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL COMMENT '排序字段',
  `status` tinyint(1) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tags
-- ----------------------------
INSERT INTO `tags` VALUES (1, 'SpringBoot', 'springboot-relevant', 1, 0, NULL, '2025-10-23 08:47:04');
INSERT INTO `tags` VALUES (2, 'Java', 'java-relevant', NULL, 0, NULL, '2025-10-23 08:37:02');
INSERT INTO `tags` VALUES (3, 'Golang', NULL, NULL, 1, NULL, '2025-10-23 08:35:35');
INSERT INTO `tags` VALUES (4, 'MySql', 'mysql', NULL, 1, NULL, '2025-10-23 08:37:17');
INSERT INTO `tags` VALUES (5, 'PostgreSQL', NULL, NULL, 1, NULL, '2025-10-23 08:35:37');
INSERT INTO `tags` VALUES (6, 'Redis', NULL, NULL, 1, NULL, '2025-10-23 08:35:39');
INSERT INTO `tags` VALUES (7, 'MongoDB', NULL, NULL, 1, NULL, '2025-10-23 08:35:43');
INSERT INTO `tags` VALUES (8, 'Elasticsearch', NULL, NULL, 1, NULL, '2025-10-23 08:35:44');
INSERT INTO `tags` VALUES (9, 'RabbitMQ', NULL, NULL, 1, NULL, '2025-10-23 08:35:45');
INSERT INTO `tags` VALUES (10, '微服务', NULL, NULL, 1, NULL, '2025-10-23 08:35:46');
INSERT INTO `tags` VALUES (11, 'DDD', NULL, NULL, 1, NULL, '2025-10-23 08:35:48');
INSERT INTO `tags` VALUES (12, 'API网关(Gateway)', NULL, NULL, 1, NULL, '2025-10-23 08:35:50');
INSERT INTO `tags` VALUES (13, '配置中心', NULL, NULL, 1, NULL, '2025-10-23 08:35:51');
INSERT INTO `tags` VALUES (14, 'JWT', NULL, NULL, 1, NULL, '2025-10-23 08:35:53');
INSERT INTO `tags` VALUES (15, 'OAuth2', NULL, NULL, 1, NULL, '2025-10-23 08:35:54');
INSERT INTO `tags` VALUES (16, 'OpenID Connect', NULL, NULL, 1, NULL, '2025-10-23 08:35:55');
INSERT INTO `tags` VALUES (17, 'CSRF', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (18, 'XSS', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (19, 'SQL注入', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (20, 'HTTPS', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (21, 'SSL', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (22, 'TLS', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (23, 'Docker', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (24, 'k8s', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (25, 'CI / CD', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (26, 'Jenkins', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (27, 'Github Actions', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (28, 'Nginx', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (29, 'Linux', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (30, 'ELK / EFK', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (31, 'Prometheus / Grafana', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (32, 'Swagger', NULL, NULL, 1, NULL, NULL);
INSERT INTO `tags` VALUES (40, '测试标签', NULL, NULL, 1, '2025-10-23 20:50:20', '2025-10-23 20:50:20');

-- ----------------------------
-- Table structure for tree_comments
-- ----------------------------
DROP TABLE IF EXISTS `tree_comments`;
CREATE TABLE `tree_comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tree_comments
-- ----------------------------
INSERT INTO `tree_comments` VALUES (1, 1, 'Hazenix', '给自己的20岁礼物', 0, '2025-11-01 20:07:34');


-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `refresh_token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'refreshToken',
  `avatar` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` tinyint(1) NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `role` tinyint(1) NULL DEFAULT NULL,
  `last_login_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'Hazenix', 'cf49935a44c06de0c6f23d87ad862895', '19559357353', '3542495583@qq.com', NULL, NULL, 1, 0, 0, '2025-11-01 21:03:26', '2025-10-13 12:33:07', NULL);

-- ----------------------------
-- Table structure for user_article
-- ----------------------------
DROP TABLE IF EXISTS `user_article`;
CREATE TABLE `user_article`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `article_id` bigint NULL DEFAULT NULL,
  `is_liked` tinyint(1) NULL DEFAULT NULL COMMENT '0未点赞  1已点赞',
  `is_favorite` tinyint(1) NULL DEFAULT NULL COMMENT '0未收藏  1已收藏',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_article
-- ----------------------------
INSERT INTO `user_article` VALUES (1, 1, 32, 0, 1);

-- ----------------------------
-- Table structure for user_social_link
-- ----------------------------
DROP TABLE IF EXISTS `user_social_link`;
CREATE TABLE `user_social_link`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `platform` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '\'WECHAT\', \'QQ\', \'GITHUB\', \'WEIBO\'',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '填写具体的联系方式',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_social_link
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
