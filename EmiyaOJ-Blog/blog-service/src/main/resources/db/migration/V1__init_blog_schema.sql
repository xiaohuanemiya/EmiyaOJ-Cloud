SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `blog` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `blog_type` tinyint NOT NULL DEFAULT '0' COMMENT '0-normal blog, 1-problem solution',
  `problem_id` bigint DEFAULT NULL COMMENT 'linked problem id for solution blogs',
  `view_count` int NOT NULL DEFAULT '0',
  `like_count` int NOT NULL DEFAULT '0',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0-pending, 1-approved, 2-rejected, 3-manual review',
  `audit_task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'latest moderation task id',
  `audit_reason` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'moderation reason',
  `audit_labels` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'moderation labels',
  `audit_time` datetime DEFAULT NULL COMMENT 'moderation time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_problem_type_deleted` (`user_id`,`problem_id`,`blog_type`,`deleted`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_problem_type` (`problem_id`,`blog_type`) USING BTREE,
  KEY `idx_audit_status` (`audit_status`) USING BTREE,
  KEY `idx_update_time` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog table';

CREATE TABLE IF NOT EXISTS `blog_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0-pending, 1-approved, 2-rejected, 3-manual review',
  `audit_task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'latest moderation task id',
  `audit_reason` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'moderation reason',
  `audit_labels` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'moderation labels',
  `audit_time` datetime DEFAULT NULL COMMENT 'moderation time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_audit_status` (`audit_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog comment table';

CREATE TABLE IF NOT EXISTS `blog_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_blog` (`user_id`,`blog_id`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog like table';

CREATE TABLE IF NOT EXISTS `blog_picture` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint DEFAULT NULL,
  `object_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `size` bigint NOT NULL,
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_object_name` (`object_name`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog picture table';

CREATE TABLE IF NOT EXISTS `blog_star` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_blog` (`user_id`,`blog_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog favorite table';

CREATE TABLE IF NOT EXISTS `blog_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog tag table';

CREATE TABLE IF NOT EXISTS `blog_tag_association` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_blog_tag` (`blog_id`,`tag_id`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog tag association table';

CREATE TABLE IF NOT EXISTS `user_blog` (
  `user_id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `blog_count` int NOT NULL DEFAULT '0',
  `star_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='user blog statistics table';

DELIMITER $$

CREATE TRIGGER `after_blog_insert` AFTER INSERT ON `blog` FOR EACH ROW
BEGIN
  IF NEW.deleted = 0 AND NEW.audit_status = 1 THEN
    UPDATE user_blog
    SET blog_count = blog_count + 1
    WHERE user_id = NEW.user_id;
  END IF;
END$$

CREATE TRIGGER `after_blog_delete` AFTER UPDATE ON `blog` FOR EACH ROW
BEGIN
  IF OLD.deleted = 0 AND OLD.audit_status = 1
      AND (NEW.deleted = 1 OR NEW.audit_status <> 1) THEN
    UPDATE user_blog
    SET blog_count = GREATEST(blog_count - 1, 0)
    WHERE user_id = NEW.user_id;
  ELSEIF NEW.deleted = 0 AND NEW.audit_status = 1
      AND (OLD.deleted = 1 OR OLD.audit_status <> 1) THEN
    UPDATE user_blog
    SET blog_count = blog_count + 1
    WHERE user_id = NEW.user_id;
  END IF;
END$$

CREATE TRIGGER `after_blog_star_insert` AFTER INSERT ON `blog_star` FOR EACH ROW
BEGIN
  UPDATE user_blog
  SET star_count = star_count + 1
  WHERE user_id = NEW.user_id;
END$$

CREATE TRIGGER `after_blog_star_delete` AFTER DELETE ON `blog_star` FOR EACH ROW
BEGIN
  UPDATE user_blog
  SET star_count = GREATEST(star_count - 1, 0)
  WHERE user_id = OLD.user_id;
END$$

DELIMITER ;
