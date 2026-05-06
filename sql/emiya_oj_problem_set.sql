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
DROP TABLE IF EXISTS `problem_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_set` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'problem set id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'description',
  `creator_id` bigint NOT NULL COMMENT 'creator user id',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-hidden, 1-public',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-normal, 1-deleted',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `create_by` bigint DEFAULT NULL COMMENT 'create user',
  `update_by` bigint DEFAULT NULL COMMENT 'update user',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='problem set';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `problem_set` WRITE;
/*!40000 ALTER TABLE `problem_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `problem_set` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `problem_set_problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_set_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `set_id` bigint NOT NULL COMMENT 'problem set id',
  `problem_id` bigint NOT NULL COMMENT 'problem id',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT 'sort order',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'note',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_set_problem` (`set_id`,`problem_id`) USING BTREE,
  KEY `idx_set_id` (`set_id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='problem set problem relation';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `problem_set_problem` WRITE;
/*!40000 ALTER TABLE `problem_set_problem` DISABLE KEYS */;
/*!40000 ALTER TABLE `problem_set_problem` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

