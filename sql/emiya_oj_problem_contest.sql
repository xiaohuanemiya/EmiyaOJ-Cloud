-- Generated from Docker MySQL container emiyaoj-mysql
-- Source: jdbc:mysql://localhost:3306/emiya_oj_problem
CREATE DATABASE IF NOT EXISTS `emiya_oj_problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_problem`;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `contest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'contest id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'description',
  `rule_type` tinyint NOT NULL COMMENT '1-ACM/ICPC, 2-IOI, 3-Codeforces',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `freeze_before_minutes` int NOT NULL DEFAULT '60' COMMENT 'rank freeze minutes before end; 0 disables freeze',
  `invite_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '10-char invite code',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-draft, 1-published, 2-cancelled',
  `creator_id` bigint NOT NULL COMMENT 'creator user id',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-normal, 1-deleted',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `create_by` bigint DEFAULT NULL COMMENT 'create user',
  `update_by` bigint DEFAULT NULL COMMENT 'update user',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_invite_code` (`invite_code`) USING BTREE,
  KEY `idx_rule_type` (`rule_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `contest` WRITE;
/*!40000 ALTER TABLE `contest` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `contest_problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'contest problem id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `problem_id` bigint NOT NULL COMMENT 'problem id',
  `label` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'display label',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT 'sort order',
  `score` int NOT NULL DEFAULT '100' COMMENT 'base score',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_contest_problem` (`contest_id`,`problem_id`) USING BTREE,
  UNIQUE KEY `uk_contest_label` (`contest_id`,`label`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest problem';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `contest_problem` WRITE;
/*!40000 ALTER TABLE `contest_problem` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest_problem` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `contest_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'registration id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'register time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_contest_user` (`contest_id`,`user_id`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest registration';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `contest_registration` WRITE;
/*!40000 ALTER TABLE `contest_registration` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest_registration` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `contest_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'admin id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `create_by` bigint DEFAULT NULL COMMENT 'operator id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_contest_admin` (`contest_id`,`user_id`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest admin';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `contest_admin` WRITE;
/*!40000 ALTER TABLE `contest_admin` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest_admin` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

