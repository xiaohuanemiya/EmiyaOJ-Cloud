CREATE DATABASE IF NOT EXISTS `emiya_oj_problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_problem`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `problem_set_problem`;
DROP TABLE IF EXISTS `problem_set`;

CREATE TABLE `problem_set` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'problem set id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'description',
  `creator_id` bigint NOT NULL COMMENT 'creator user id',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-hidden, 1-public',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-normal, 1-deleted',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `create_by` bigint NULL DEFAULT NULL COMMENT 'create user',
  `update_by` bigint NULL DEFAULT NULL COMMENT 'update user',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator_id` (`creator_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'problem set' ROW_FORMAT = Dynamic;

CREATE TABLE `problem_set_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `set_id` bigint NOT NULL COMMENT 'problem set id',
  `problem_id` bigint NOT NULL COMMENT 'problem id',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT 'sort order',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'note',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_set_problem` (`set_id`, `problem_id`) USING BTREE,
  INDEX `idx_set_id` (`set_id`) USING BTREE,
  INDEX `idx_problem_id` (`problem_id`) USING BTREE,
  INDEX `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'problem set problem relation' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
