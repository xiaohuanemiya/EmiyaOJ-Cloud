-- Generated from Docker MySQL container emiyaoj-mysql
-- Source: jdbc:mysql://localhost:3306/emiya_oj_judge
CREATE DATABASE IF NOT EXISTS `emiya_oj_judge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_judge`;


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
DROP TABLE IF EXISTS `submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `contest_id` bigint DEFAULT NULL COMMENT '竞赛ID，普通提交为空',
  `contest_problem_id` bigint DEFAULT NULL COMMENT '竞赛题目关联ID，普通提交为空',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `language_id` bigint NOT NULL COMMENT '语言ID',
  `code` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源代码',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_contest_problem_id` (`contest_problem_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_language_id` (`language_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2051841831544086530 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='提交记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `submission` WRITE;
/*!40000 ALTER TABLE `submission` DISABLE KEYS */;
/*!40000 ALTER TABLE `submission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `submission_case_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission_case_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细结果ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `test_case_id` bigint NOT NULL COMMENT '测试用例ID',
  `case_order` int DEFAULT '0' COMMENT '测试用例执行顺序',
  `status` int NOT NULL COMMENT '判题状态：2-AC, 4-SE, 5-WA, 6-TLE, 7-MLE, 8-RE, 9-OLE',
  `score` int DEFAULT '0' COMMENT '该测试用例获得分值',
  `time_used` bigint DEFAULT '0' COMMENT '运行时间（毫秒）',
  `memory_used` bigint DEFAULT '0' COMMENT '运行内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_submission_id` (`submission_id`) USING BTREE,
  KEY `idx_test_case_id` (`test_case_id`) USING BTREE,
  KEY `idx_submission_order` (`submission_id`,`case_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='测试用例判题明细结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `submission_case_result` WRITE;
/*!40000 ALTER TABLE `submission_case_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `submission_case_result` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `submission_judge_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission_judge_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `status` int NOT NULL DEFAULT '0' COMMENT '判题状态：0-Pending, 1-Judging, 2-AC, 3-CE, 4-SE, 5-WA, 6-TLE, 7-MLE, 8-RE, 9-OLE, 10-PA',
  `passed_case_count` int NOT NULL DEFAULT '0' COMMENT '通过测试用例数量',
  `total_case_count` int NOT NULL DEFAULT '0' COMMENT '测试用例总数',
  `score` int DEFAULT '0' COMMENT '得分(0~100)',
  `max_time_used` bigint DEFAULT '0' COMMENT '最高运行时间（毫秒）',
  `max_memory_used` bigint DEFAULT '0' COMMENT '最高运行内存（KB）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `compile_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '编译错误信息',
  `finish_time` datetime DEFAULT NULL COMMENT '判题完成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_submission_id` (`submission_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_finish_time` (`finish_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='提交判题汇总结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `submission_judge_result` WRITE;
/*!40000 ALTER TABLE `submission_judge_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `submission_judge_result` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

