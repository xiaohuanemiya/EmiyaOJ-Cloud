-- =====================================================
-- EmiyaOJ 判题服务数据库 (emiya_oj_judge)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `emiya_oj_judge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_judge`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 提交记录表
-- ----------------------------
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `language_id` bigint NOT NULL COMMENT '语言ID',
  `code` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源代码',
  `status` int NOT NULL DEFAULT 0 COMMENT '判题状态：0-待判题, 1-判题中, 2-已完成, 3-编译错误, 4-系统错误',
  `score` int NULL DEFAULT 0 COMMENT '得分(0~100)',
  `time_used` bigint NULL DEFAULT 0 COMMENT '最大运行时间（毫秒）',
  `memory_used` bigint NULL DEFAULT 0 COMMENT '最大运行内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '运行错误信息',
  `compile_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '编译错误信息',
  `pass_rate` double NULL DEFAULT 0 COMMENT '通过率(0.0~1.0)',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_problem_id`(`problem_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_language_id`(`language_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提交记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 测试用例判题结果表
-- ----------------------------
DROP TABLE IF EXISTS `submission_result`;
CREATE TABLE `submission_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `test_case_id` bigint NOT NULL COMMENT '测试用例ID',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '判题状态',
  `time_used` int NULL DEFAULT 0 COMMENT '使用时间（毫秒）',
  `memory_used` int NULL DEFAULT 0 COMMENT '使用内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_test_case_id`(`test_case_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试用例判题结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 本地消息表 (分布式事务可靠性保障)
-- ----------------------------
DROP TABLE IF EXISTS `message_event`;
CREATE TABLE `message_event`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型: JUDGE_SUBMIT',
  `business_id` bigint NOT NULL COMMENT '关联业务ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '消息状态: 0-待处理, 1-处理中, 2-处理成功, 3-处理失败',
  `retry_count` int NULL DEFAULT 0 COMMENT '已重试次数',
  `max_retry_count` int NULL DEFAULT 3 COMMENT '最大重试次数',
  `payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容(JSON)',
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下次重试时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_business_id`(`business_id` ASC) USING BTREE,
  INDEX `idx_next_retry_time`(`next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '本地消息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
