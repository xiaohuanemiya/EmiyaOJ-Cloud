-- =====================================================
-- EmiyaOJ 判题服务数据库 (emiya_oj_judge)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `emiya_oj_judge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_judge`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 提交记录表：只保存提交元信息
-- ----------------------------
DROP TABLE IF EXISTS `submission_case_result`;
DROP TABLE IF EXISTS `submission_judge_result`;
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `language_id` bigint NOT NULL COMMENT '语言ID',
  `code` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源代码',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_problem_id` (`problem_id` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_language_id` (`language_id` ASC) USING BTREE,
  INDEX `idx_create_time` (`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提交记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 提交判题汇总结果表：每次提交一条
-- ----------------------------
CREATE TABLE `submission_judge_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '判题状态：0-Pending, 1-Judging, 2-AC, 3-CE, 4-SE, 5-WA, 6-TLE, 7-MLE, 8-RE, 9-OLE, 10-PA',
  `passed_case_count` int NOT NULL DEFAULT 0 COMMENT '通过测试用例数量',
  `total_case_count` int NOT NULL DEFAULT 0 COMMENT '测试用例总数',
  `score` int NULL DEFAULT 0 COMMENT '得分(0~100)',
  `max_time_used` bigint NULL DEFAULT 0 COMMENT '最高运行时间（毫秒）',
  `max_memory_used` bigint NULL DEFAULT 0 COMMENT '最高运行内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `compile_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '编译错误信息',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '判题完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id` (`submission_id` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  INDEX `idx_finish_time` (`finish_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提交判题汇总结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 测试用例判题明细结果表：每次提交每个已运行用例一条
-- ----------------------------
CREATE TABLE `submission_case_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细结果ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `test_case_id` bigint NOT NULL COMMENT '测试用例ID',
  `case_order` int NULL DEFAULT 0 COMMENT '测试用例执行顺序',
  `status` int NOT NULL COMMENT '判题状态：2-AC, 4-SE, 5-WA, 6-TLE, 7-MLE, 8-RE, 9-OLE',
  `score` int NULL DEFAULT 0 COMMENT '该测试用例获得分值',
  `time_used` bigint NULL DEFAULT 0 COMMENT '运行时间（毫秒）',
  `memory_used` bigint NULL DEFAULT 0 COMMENT '运行内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_id` (`submission_id` ASC) USING BTREE,
  INDEX `idx_test_case_id` (`test_case_id` ASC) USING BTREE,
  INDEX `idx_submission_order` (`submission_id` ASC, `case_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试用例判题明细结果表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
