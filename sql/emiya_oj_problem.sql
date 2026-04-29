-- =====================================================
-- EmiyaOJ 题目服务数据库 (emiya_oj_problem)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `emiya_oj_problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_problem`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 题目表
-- ----------------------------
DROP TABLE IF EXISTS `problem`;
CREATE TABLE `problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目描述',
  `input_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输入描述',
  `output_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输出描述',
  `sample_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '样例输入',
  `sample_output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '样例输出',
  `hint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提示信息',
  `difficulty` tinyint NULL DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `time_limit` int NOT NULL COMMENT 'CPU时间限制（毫秒）',
  `memory_limit` int NOT NULL COMMENT '内存限制（MB）',
  `stack_limit` int NULL DEFAULT 128 COMMENT '栈内存限制（MB）',
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目来源',
  `author_id` bigint NULL DEFAULT NULL COMMENT '出题人ID',
  `accept_count` int NULL DEFAULT 0 COMMENT '通过次数',
  `submit_count` int NULL DEFAULT 0 COMMENT '提交次数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-公开',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_difficulty` (`difficulty` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  INDEX `idx_author_id` (`author_id` ASC) USING BTREE,
  INDEX `idx_create_time` (`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签描述',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#409EFF' COMMENT '标签颜色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name` (`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 题目标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `problem_tag`;
CREATE TABLE `problem_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_problem_tag` (`problem_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_problem_id` (`problem_id` ASC) USING BTREE,
  INDEX `idx_tag_id` (`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 测试用例表
-- ----------------------------
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `input` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '输入数据',
  `output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预期输出',
  `is_sample` tinyint NULL DEFAULT 0 COMMENT '是否为样例：0-否，1-是',
  `score` int NULL DEFAULT 0 COMMENT '分值',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_problem_id` (`problem_id` ASC) USING BTREE,
  INDEX `idx_is_sample` (`is_sample` ASC) USING BTREE,
  INDEX `idx_sort_order` (`sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试用例表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
