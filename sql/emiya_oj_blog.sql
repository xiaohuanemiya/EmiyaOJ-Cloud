-- =====================================================
-- EmiyaOJ Blog service database (emiya_oj_blog)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `emiya_oj_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `emiya_oj_blog`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Blog table
-- blog_type: 0 normal blog, 1 problem solution
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `blog_type` tinyint NOT NULL DEFAULT 0 COMMENT '0-normal blog, 1-problem solution',
  `problem_id` bigint NULL DEFAULT NULL COMMENT 'linked problem id for solution blogs',
  `view_count` int NOT NULL DEFAULT 0,
  `like_count` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_problem_type_deleted` (`user_id` ASC, `problem_id` ASC, `blog_type` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_problem_type` (`problem_id` ASC, `blog_type` ASC) USING BTREE,
  INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog comment table
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_blog_id` (`blog_id` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog comment table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog picture table
-- ----------------------------
DROP TABLE IF EXISTS `blog_picture`;
CREATE TABLE `blog_picture` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NULL DEFAULT NULL,
  `object_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `size` bigint NOT NULL,
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_object_name` (`object_name` ASC) USING BTREE,
  INDEX `idx_blog_id` (`blog_id` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog picture table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog favorite table
-- ----------------------------
DROP TABLE IF EXISTS `blog_star`;
CREATE TABLE `blog_star` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_blog` (`user_id` ASC, `blog_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog favorite table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog like table
-- ----------------------------
DROP TABLE IF EXISTS `blog_like`;
CREATE TABLE `blog_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_blog` (`user_id` ASC, `blog_id` ASC) USING BTREE,
  INDEX `idx_blog_id` (`blog_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog like table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog tag table
-- ----------------------------
DROP TABLE IF EXISTS `blog_tag`;
CREATE TABLE `blog_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog tag table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Blog tag association table
-- ----------------------------
DROP TABLE IF EXISTS `blog_tag_association`;
CREATE TABLE `blog_tag_association` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_blog_tag` (`blog_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_blog_id` (`blog_id` ASC) USING BTREE,
  INDEX `idx_tag_id` (`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'blog tag association table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- User blog statistics table
-- ----------------------------
DROP TABLE IF EXISTS `user_blog`;
CREATE TABLE `user_blog` (
  `user_id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `blog_count` int NOT NULL DEFAULT 0,
  `star_count` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'user blog statistics table' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- Triggers
-- =====================================================

DROP TRIGGER IF EXISTS `after_blog_insert`;
delimiter ;;
CREATE TRIGGER `after_blog_insert` AFTER INSERT ON `blog` FOR EACH ROW BEGIN
    UPDATE user_blog
    SET blog_count = blog_count + 1
    WHERE user_id = NEW.user_id;
END
;;
delimiter ;

DROP TRIGGER IF EXISTS `after_blog_delete`;
delimiter ;;
CREATE TRIGGER `after_blog_delete` AFTER UPDATE ON `blog` FOR EACH ROW BEGIN
    IF NEW.deleted = 1 AND OLD.deleted = 0 THEN
        UPDATE user_blog
        SET blog_count = GREATEST(blog_count - 1, 0)
        WHERE user_id = NEW.user_id;
    END IF;
END
;;
delimiter ;

DROP TRIGGER IF EXISTS `after_blog_star_insert`;
delimiter ;;
CREATE TRIGGER `after_blog_star_insert` AFTER INSERT ON `blog_star` FOR EACH ROW BEGIN
    UPDATE user_blog
    SET star_count = star_count + 1
    WHERE user_id = NEW.user_id;
END
;;
delimiter ;

DROP TRIGGER IF EXISTS `after_blog_star_delete`;
delimiter ;;
CREATE TRIGGER `after_blog_star_delete` AFTER DELETE ON `blog_star` FOR EACH ROW BEGIN
    UPDATE user_blog
    SET star_count = GREATEST(star_count - 1, 0)
    WHERE user_id = OLD.user_id;
END
;;
delimiter ;
