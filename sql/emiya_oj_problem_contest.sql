CREATE DATABASE IF NOT EXISTS `emiya_oj_problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_problem`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `contest_admin`;
DROP TABLE IF EXISTS `contest_registration`;
DROP TABLE IF EXISTS `contest_problem`;
DROP TABLE IF EXISTS `contest`;

CREATE TABLE `contest` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'contest id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'description',
  `rule_type` tinyint NOT NULL COMMENT '1-ACM/ICPC, 2-IOI, 3-Codeforces',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `freeze_before_minutes` int NOT NULL DEFAULT 60 COMMENT 'rank freeze minutes before end; 0 disables freeze',
  `invite_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '10-char invite code',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-draft, 1-published, 2-cancelled',
  `creator_id` bigint NOT NULL COMMENT 'creator user id',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-normal, 1-deleted',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `create_by` bigint NULL DEFAULT NULL COMMENT 'create user',
  `update_by` bigint NULL DEFAULT NULL COMMENT 'update user',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invite_code` (`invite_code`) USING BTREE,
  INDEX `idx_rule_type` (`rule_type`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_start_time` (`start_time`) USING BTREE,
  INDEX `idx_creator_id` (`creator_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'contest' ROW_FORMAT = Dynamic;

CREATE TABLE `contest_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'contest problem id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `problem_id` bigint NOT NULL COMMENT 'problem id',
  `label` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'display label',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT 'sort order',
  `score` int NOT NULL DEFAULT 100 COMMENT 'base score',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_problem` (`contest_id`, `problem_id`) USING BTREE,
  UNIQUE INDEX `uk_contest_label` (`contest_id`, `label`) USING BTREE,
  INDEX `idx_contest_id` (`contest_id`) USING BTREE,
  INDEX `idx_problem_id` (`problem_id`) USING BTREE,
  INDEX `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'contest problem' ROW_FORMAT = Dynamic;

CREATE TABLE `contest_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'registration id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'register time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_user` (`contest_id`, `user_id`) USING BTREE,
  INDEX `idx_contest_id` (`contest_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'contest registration' ROW_FORMAT = Dynamic;

CREATE TABLE `contest_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'admin id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `create_by` bigint NULL DEFAULT NULL COMMENT 'operator id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_admin` (`contest_id`, `user_id`) USING BTREE,
  INDEX `idx_contest_id` (`contest_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'contest admin' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

CREATE DATABASE IF NOT EXISTS `emiya_oj_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_auth`;

INSERT INTO `permission`
(`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES
(0, 'CONTEST', 'Contest Management', 1, '/contest', 'contest/index', 'trophy', 40, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `permission_name` = VALUES(`permission_name`),
  `permission_type` = VALUES(`permission_type`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `deleted` = VALUES(`deleted`),
  `update_time` = NOW();
