-- Generated from Docker MySQL container emiyaoj-mysql
-- Source: jdbc:mysql://localhost:3306/emiya_oj_auth
CREATE DATABASE IF NOT EXISTS `emiya_oj_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_auth`;


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
DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模块标题',
  `business_type` tinyint DEFAULT NULL COMMENT '业务类型',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方式',
  `operator_type` tinyint DEFAULT '0' COMMENT '操作类别',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人员',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求URL',
  `oper_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作地点',
  `oper_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `json_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '返回参数',
  `status` tinyint DEFAULT '0' COMMENT '操作状态',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_title` (`title`) USING BTREE,
  KEY `idx_business_type` (`business_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_oper_time` (`oper_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID',
  `permission_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` tinyint NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_permission_code` (`permission_code`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_permission_type` (`permission_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,0,'DASHBOARD','仪表盘',1,'/dashboard','views/dashboard/index.vue','HomeFilled',0,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(2,0,'USER.LIST','用户管理',1,'/user','views/user/index.vue','User',1,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(3,2,'USER.ADD','新增用户',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(4,2,'USER.EDIT','编辑用户',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(5,2,'USER.DELETE','删除用户',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(6,0,'ROLE.LIST','角色管理',1,'/role','views/role/index.vue','UserFilled',2,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(7,6,'ROLE.ADD','新增角色',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(8,6,'ROLE.EDIT','编辑角色',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(9,6,'ROLE.DELETE','删除角色',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(10,6,'ROLE.ASSIGN','分配权限',2,NULL,NULL,NULL,3,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(11,0,'PERMISSION.LIST','权限管理',1,'/permission','views/permission/index.vue','Lock',3,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(12,11,'PERMISSION.ADD','新增权限',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(13,11,'PERMISSION.EDIT','编辑权限',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(14,11,'PERMISSION.DELETE','删除权限',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(15,0,'PROBLEM.LIST','题目管理',1,'/problem','views/problem/index.vue','Document',4,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(16,15,'PROBLEM.ADD','新增题目',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(17,15,'PROBLEM.EDIT','编辑题目',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(18,15,'PROBLEM.DELETE','删除题目',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(19,15,'TESTCASE.LIST','查看测试用例',2,NULL,NULL,NULL,3,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(20,15,'TESTCASE.ADD','新增测试用例',2,NULL,NULL,NULL,4,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(21,15,'TESTCASE.EDIT','编辑测试用例',2,NULL,NULL,NULL,5,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(22,15,'TESTCASE.DELETE','删除测试用例',2,NULL,NULL,NULL,6,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(23,0,'PROBLEM_SET.LIST','题单管理',1,'/problem-set','views/problemSet/index.vue','Collection',5,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(24,38,'CONTEST.LIST','竞赛管理',1,'/contest','views/contest/index.vue','Trophy',6,1,0,'2026-05-06 13:15:09','2026-05-06 06:55:22',NULL,NULL),(25,0,'LANGUAGE.LIST','语言管理',1,'/language','views/language/index.vue','Setting',7,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(26,25,'LANGUAGE.ADD','新增语言',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(27,25,'LANGUAGE.EDIT','编辑语言',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(28,25,'LANGUAGE.DELETE','删除语言',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(29,0,'BLOG.LIST','博客管理',1,'/blog','views/blog/index.vue','EditPen',8,1,0,'2026-05-06 13:15:09','2026-05-06 13:15:09',NULL,NULL),(30,29,'BLOG.ADD','新增博客',2,NULL,NULL,NULL,0,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(31,29,'BLOG.EDIT','编辑博客',2,NULL,NULL,NULL,1,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(32,29,'BLOG.DELETE','删除博客',2,NULL,NULL,NULL,2,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(33,29,'BLOG.TAG.LIST','标签列表',2,NULL,NULL,NULL,3,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(34,29,'BLOG.TAG.ADD','新增标签',2,NULL,NULL,NULL,4,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(35,29,'BLOG.TAG.EDIT','编辑标签',2,NULL,NULL,NULL,5,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(36,29,'BLOG.TAG.DELETE','删除标签',2,NULL,NULL,NULL,6,1,0,'2026-05-06 13:18:54','2026-05-06 13:18:54',NULL,NULL),(37,0,'SUBMISSION.LIST','判题管理',1,'/submission','views/submission/index.vue','List',9,1,0,'2026-05-06 13:18:48','2026-05-06 13:18:48',NULL,NULL),(38,0,'CONTEST','竞赛管理',1,'/contest','contest/index','trophy',40,1,0,'2026-05-06 12:48:31','2026-05-06 06:55:13',NULL,NULL),(39,0,'BLOG_MODERATION_MANAGE','博客审核',3,'/blog/moderation/**',NULL,NULL,41,1,0,'2026-05-06 12:48:31','2026-05-06 06:54:45',NULL,NULL);
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_code` (`role_code`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'admin','admin',NULL,1,0,'2026-05-06 13:19:41','2026-05-06 13:19:41',NULL,NULL);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_permission_id` (`permission_id`) USING BTREE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
INSERT INTO `role_permission` VALUES (1,1,1,'2026-05-06 05:34:44',NULL),(2,1,2,'2026-05-06 05:34:44',NULL),(3,1,3,'2026-05-06 05:34:44',NULL),(4,1,4,'2026-05-06 05:34:44',NULL),(5,1,5,'2026-05-06 05:34:44',NULL),(6,1,6,'2026-05-06 05:34:44',NULL),(7,1,7,'2026-05-06 05:34:44',NULL),(8,1,8,'2026-05-06 05:34:44',NULL),(9,1,9,'2026-05-06 05:34:44',NULL),(10,1,10,'2026-05-06 05:34:44',NULL),(11,1,11,'2026-05-06 05:34:44',NULL),(12,1,12,'2026-05-06 05:34:44',NULL),(13,1,13,'2026-05-06 05:34:44',NULL),(14,1,14,'2026-05-06 05:34:44',NULL),(15,1,15,'2026-05-06 05:34:44',NULL),(16,1,16,'2026-05-06 05:34:44',NULL),(17,1,17,'2026-05-06 05:34:44',NULL),(18,1,18,'2026-05-06 05:34:44',NULL),(19,1,19,'2026-05-06 05:34:44',NULL),(20,1,20,'2026-05-06 05:34:44',NULL),(21,1,21,'2026-05-06 05:34:44',NULL),(22,1,22,'2026-05-06 05:34:44',NULL),(23,1,23,'2026-05-06 05:34:44',NULL),(24,1,24,'2026-05-06 05:34:44',NULL),(25,1,25,'2026-05-06 05:34:44',NULL),(26,1,26,'2026-05-06 05:34:44',NULL),(27,1,27,'2026-05-06 05:34:44',NULL),(28,1,28,'2026-05-06 05:34:44',NULL),(29,1,29,'2026-05-06 05:34:44',NULL),(30,1,30,'2026-05-06 05:34:44',NULL),(31,1,31,'2026-05-06 05:34:44',NULL),(32,1,32,'2026-05-06 05:34:44',NULL),(33,1,33,'2026-05-06 05:34:44',NULL),(34,1,34,'2026-05-06 05:34:44',NULL),(35,1,35,'2026-05-06 05:34:44',NULL),(36,1,36,'2026-05-06 05:34:44',NULL),(37,1,37,'2026-05-06 05:34:44',NULL),(38,1,38,'2026-05-06 05:34:44',NULL),(39,1,39,'2026-05-06 05:34:44',NULL);
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  UNIQUE KEY `uk_email` (`email`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2051898437005271042 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (2051888299500380162,'admintestuser','$2a$10$WTfOHCfB4YNLPMr1YzxpheHi4dByvAlWH9CwqCGeIA8Vr1KUwsgm6','Admin Test User',NULL,NULL,NULL,1,0,'2026-05-06 12:54:27','2026-05-06 12:54:27',NULL,NULL),(2051898437005271041,'DefaultAdmin','$2a$10$GYoiIOe5Ecw.lvXKtQJe4OeEi.seEWxYfxpRAckh3oYE06Wvi0v1y','Default Admin',NULL,NULL,NULL,1,0,'2026-05-06 05:34:45','2026-05-06 05:34:45',NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,2051898437005271041,1,'2026-05-06 05:34:45',NULL);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- Generated from Docker MySQL container emiyaoj-mysql
-- Source: jdbc:mysql://localhost:3306/emiya_oj_blog
CREATE DATABASE IF NOT EXISTS `emiya_oj_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_blog`;


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
DROP TABLE IF EXISTS `blog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog` (
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog` WRITE;
/*!40000 ALTER TABLE `blog` DISABLE KEYS */;
INSERT INTO `blog` VALUES (1,2051898437005271041,'111','111',0,NULL,0,0,3,'7cc3de119d3e4487b8f32b958cd4888b','ScanText exception: code: 403, Specified api is not purchased, open the link to purchase api: https://help.aliyun.com/document_detail/465341.html request id: B046DF70-8434-5EE1-A879-CF55720DD908','','2026-05-06 05:39:44','2026-05-06 05:39:43','2026-05-06 05:39:44',0),(2,2051898437005271041,'11111','111',0,NULL,0,0,3,'b106ba3a6c384ff8b5809804c1e225ee','ScanText exception: code: 403, Specified api is not purchased, open the link to purchase api: https://help.aliyun.com/document_detail/465341.html request id: 1248374D-CB99-53C4-BB28-BCA295689213','','2026-05-06 05:42:57','2026-05-06 05:42:58','2026-05-06 05:42:58',0),(3,2051898437005271041,'123321','123',0,NULL,0,0,3,'c531eae66cf54dc49eb4ef9ec780e2f6','ScanText exception: code: 403, Specified api is not purchased, open the link to purchase api: https://help.aliyun.com/document_detail/465341.html request id: 0EBA97B9-491D-5CB7-B55B-F81D0CC7FB87','','2026-05-06 05:43:23','2026-05-06 05:43:23','2026-05-06 05:43:23',0),(4,2051898437005271041,'3213123123','3123213',0,NULL,0,0,3,'9c8a7a4170b740c1bba7746524a5b8a7','ScanText exception: code: 403, Specified api is not purchased, open the link to purchase api: https://help.aliyun.com/document_detail/465341.html request id: B3E0CDBD-C03C-5763-B71E-23FF401D8214','','2026-05-06 05:47:28','2026-05-06 05:47:28','2026-05-06 05:47:28',0),(5,2051898437005271041,'1','1',0,NULL,0,0,3,'7222f863b00d4bb7890f2b0bd2ce5430','ScanText exception: code: 403, Specified api is not purchased, open the link to purchase api: https://help.aliyun.com/document_detail/465341.html request id: 99BBADF4-40AD-5602-A929-B435EF98F195','','2026-05-06 06:01:43','2026-05-06 06:01:43','2026-05-06 06:01:44',0),(6,2051898437005271041,'test','test',0,NULL,0,0,1,'e6592c9c697c40c0b3dd6d8219bcc268','ScanText pass','normal','2026-05-06 06:32:14','2026-05-06 06:32:14','2026-05-06 06:32:15',0),(7,2051898437005271041,'ttt','曹尼玛',0,NULL,0,0,2,'4856b1c6d63c41ae91ae1ed000bcd813','suggestion=block; labels=abuse; contexts=title:tttcontent:曹尼玛','abuse','2026-05-06 06:32:45','2026-05-06 06:32:45','2026-05-06 06:32:45',0),(8,2051898437005271041,'213213','需要人工复核',0,NULL,0,0,1,'aa7f0f4247d94cd9979d161b006df214','ScanText pass','normal','2026-05-06 06:35:22','2026-05-06 06:35:22','2026-05-06 06:35:23',0);
/*!40000 ALTER TABLE `blog` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `after_blog_insert` AFTER INSERT ON `blog` FOR EACH ROW BEGIN
    IF NEW.deleted = 0 AND NEW.audit_status = 1 THEN
        UPDATE user_blog
        SET blog_count = blog_count + 1
        WHERE user_id = NEW.user_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `after_blog_delete` AFTER UPDATE ON `blog` FOR EACH ROW BEGIN
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
DROP TABLE IF EXISTS `blog_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_comment` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_comment` WRITE;
/*!40000 ALTER TABLE `blog_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_comment` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `blog_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_blog` (`user_id`,`blog_id`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog like table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_like` WRITE;
/*!40000 ALTER TABLE `blog_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_like` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `blog_picture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_picture` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_picture` WRITE;
/*!40000 ALTER TABLE `blog_picture` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_picture` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `blog_star`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_star` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `blog_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_blog` (`user_id`,`blog_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog favorite table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_star` WRITE;
/*!40000 ALTER TABLE `blog_star` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_star` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `after_blog_star_insert` AFTER INSERT ON `blog_star` FOR EACH ROW BEGIN
    UPDATE user_blog
    SET star_count = star_count + 1
    WHERE user_id = NEW.user_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `after_blog_star_delete` AFTER DELETE ON `blog_star` FOR EACH ROW BEGIN
    UPDATE user_blog
    SET star_count = GREATEST(star_count - 1, 0)
    WHERE user_id = OLD.user_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
DROP TABLE IF EXISTS `blog_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog tag table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_tag` WRITE;
/*!40000 ALTER TABLE `blog_tag` DISABLE KEYS */;
INSERT INTO `blog_tag` VALUES (1,'123','321');
/*!40000 ALTER TABLE `blog_tag` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `blog_tag_association`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog_tag_association` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_blog_tag` (`blog_id`,`tag_id`) USING BTREE,
  KEY `idx_blog_id` (`blog_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='blog tag association table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `blog_tag_association` WRITE;
/*!40000 ALTER TABLE `blog_tag_association` DISABLE KEYS */;
/*!40000 ALTER TABLE `blog_tag_association` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `user_blog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_blog` (
  `user_id` bigint NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `blog_count` int NOT NULL DEFAULT '0',
  `star_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='user blog statistics table';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_blog` WRITE;
/*!40000 ALTER TABLE `user_blog` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_blog` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


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
INSERT INTO `submission` VALUES (2049510810584084481,1,NULL,NULL,2044312993243619329,1,'#include<bits/stdc++.h>\r\nusing namespace std;\r\n\r\nint main(){\r\n    int a,b;\r\n    cin>>a>>b;\r\n    cout<<a+b<<endl;\r\n    return 0;\r\n}',0,'2026-04-29 15:27:10','2026-04-29 15:27:10'),(2051841831544086529,1,NULL,NULL,2044312993243619329,1,'#include<iostream>\r\nusing namespace std;\r\n\r\nint main(){\r\n    int a,b;\r\n    cin>>a>>b;\r\n    cout<<a+b<<endl;\r\n    return 0;\r\n}',0,'2026-05-06 01:49:49','2026-05-06 01:49:49');
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
INSERT INTO `submission_case_result` VALUES (1,2049510810584084481,1,1,2,0,2,512,NULL,NULL),(2,2049510810584084481,3,2,2,10,1,512,NULL,NULL),(3,2051841831544086529,1,1,2,0,1,1788,NULL,NULL),(4,2051841831544086529,3,2,2,10,0,512,NULL,NULL);
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
INSERT INTO `submission_judge_result` VALUES (1,2049510810584084481,2,2,2,100,2,512,NULL,NULL,'2026-04-29 15:27:13',NULL,'2026-04-29 15:27:13'),(2,2051841831544086529,2,2,2,100,1,1788,NULL,NULL,'2026-05-06 01:49:50',NULL,'2026-05-06 01:49:50');
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
INSERT INTO `contest` VALUES (1,'test','',1,'2026-05-04 00:52:00','2026-05-30 00:00:00',0,'Q*k&GepQQG',1,2044312993243619329,0,'2026-04-29 14:58:01','2026-05-03 16:51:49',2044312993243619329,2044312993243619329);
/*!40000 ALTER TABLE `contest` ENABLE KEYS */;
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
INSERT INTO `contest_admin` VALUES (1,1,2044312993243619329,'2026-04-29 14:58:01',2044312993243619329);
/*!40000 ALTER TABLE `contest_admin` ENABLE KEYS */;
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
INSERT INTO `contest_problem` VALUES (7,1,1,'A',1,100,'2026-05-03 16:51:49');
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
INSERT INTO `contest_registration` VALUES (3,1,2046841090203357186,'2026-05-03 16:51:57');
/*!40000 ALTER TABLE `contest_registration` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `language` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '语言ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言名称，如 C++、Java、Python3',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示版本，如 C++20、Java 21',
  `language_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '命令模板中的语言版本值，如 c++20、c11',
  `compile_file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'main' COMMENT '源文件基础名，不含扩展名',
  `source_file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源文件扩展名，不含点',
  `executable_file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'main' COMMENT '运行命令中的可执行目标名',
  `compiled_file_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '编译产物文件名，多个用英文逗号分隔；为空时使用 executable_file_name',
  `compile_command` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '编译命令模板，支持 {LanguageVersion}/{CompileFileName}/{SourceFileName}/{ExecutableFileName}',
  `run_command` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运行命令模板，支持 {LanguageVersion}/{CompileFileName}/{SourceFileName}/{ExecutableFileName}',
  `env_vars` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'GoJudge 环境变量，逗号或换行分隔',
  `is_compiled` tinyint DEFAULT '1' COMMENT '是否需要编译：0-否，1-是',
  `time_limit_multiplier` decimal(5,2) DEFAULT '1.00' COMMENT '运行 CPU 时间限制倍数',
  `memory_limit_multiplier` decimal(5,2) DEFAULT '1.00' COMMENT '运行内存限制倍数',
  `compile_time_limit` int DEFAULT '10000' COMMENT '编译 CPU 时间限制（毫秒）',
  `compile_memory_limit` int DEFAULT '512' COMMENT '编译内存限制（MB）',
  `compile_proc_limit` int DEFAULT '50' COMMENT '编译进程数限制',
  `run_proc_limit` int DEFAULT '1' COMMENT '运行进程数限制',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name_version` (`name`,`version`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='编程语言配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `language` WRITE;
/*!40000 ALTER TABLE `language` DISABLE KEYS */;
INSERT INTO `language` VALUES (1,'C++','C++20','c++20','main','cpp','main','main','/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp','./{ExecutableFileName}','PATH=/usr/bin:/bin',1,1.00,1.00,10000,512,50,1,1,'2026-04-29 19:58:49','2026-04-29 19:58:49'),(2,'C','C11','c11','main','c','main','main','/usr/bin/gcc -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.c -lm','./{ExecutableFileName}','PATH=/usr/bin:/bin',1,1.00,1.00,10000,256,50,1,1,'2026-04-29 19:58:49','2026-04-29 19:58:49'),(3,'Java','Java 21','21','Main','java','Main','Main.class','/usr/bin/javac {CompileFileName}.java','/usr/bin/java {ExecutableFileName}','PATH=/usr/bin:/bin',1,2.00,2.00,10000,512,50,30,1,'2026-04-29 19:58:49','2026-04-29 13:23:16'),(4,'Python3','Python 3.13','3.13','main','py','main.py',NULL,NULL,'/usr/bin/python3 {SourceFileName}','PATH=/usr/bin:/bin',0,3.00,2.00,10000,256,10,1,1,'2026-04-29 19:58:49','2026-04-29 13:00:56'),(5,'Go','Go 1.24','1.24','main','go','main','main','/usr/bin/go build -o {ExecutableFileName} {CompileFileName}.go','./{ExecutableFileName}','PATH=/usr/bin:/bin,GOPATH=/tmp/go,GOCACHE=/tmp/go-cache',1,1.00,1.00,15000,512,50,5,1,'2026-04-29 19:58:49','2026-04-29 13:23:05'),(6,'C','C17','c17','main','c','main','main','/usr/bin/gcc -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.c -lm','./{ExecutableFileName}','PATH=/usr/bin:/bin',1,1.00,1.00,10000,256,50,1,1,'2026-04-29 12:55:22','2026-04-29 12:55:22');
/*!40000 ALTER TABLE `language` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目描述',
  `input_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '输入描述',
  `output_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '输出描述',
  `sample_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '样例输入',
  `sample_output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '样例输出',
  `hint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '提示信息',
  `difficulty` tinyint DEFAULT '1' COMMENT '难度：1-简单，2-中等，3-困难',
  `time_limit` int NOT NULL COMMENT 'CPU时间限制（毫秒）',
  `memory_limit` int NOT NULL COMMENT '内存限制（MB）',
  `stack_limit` int DEFAULT '128' COMMENT '栈内存限制（MB）',
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '题目来源',
  `author_id` bigint DEFAULT NULL COMMENT '出题人ID',
  `accept_count` int DEFAULT '0' COMMENT '通过次数',
  `submit_count` int DEFAULT '0' COMMENT '提交次数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-隐藏，1-公开',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_difficulty` (`difficulty`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_author_id` (`author_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='题目表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `problem` WRITE;
/*!40000 ALTER TABLE `problem` DISABLE KEYS */;
INSERT INTO `problem` VALUES (1,'A+B','输入两个数字,输出两数之和','','','1 2','3','',1,1000,1024,128,'',2044312993243619329,0,0,1,0,'2026-04-15 09:55:28','2026-04-15 11:24:04',2044312993243619329,2044312993243619329),(2,'tqw','qewqwe','','','','','',1,1000,256,128,'',2044312993243619329,0,0,1,0,'2026-05-03 16:59:57','2026-05-03 16:59:57',2044312993243619329,2044312993243619329);
/*!40000 ALTER TABLE `problem` ENABLE KEYS */;
UNLOCK TABLES;
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
INSERT INTO `problem_set` VALUES (1,'test','',2044312993243619329,1,0,'2026-04-29 15:14:10','2026-04-29 15:14:10',2044312993243619329,2044312993243619329),(2,'123321','123231',2044312993243619329,0,0,'2026-05-03 16:35:02','2026-05-03 16:35:02',2044312993243619329,2044312993243619329),(3,'123123','',2046841090203357186,1,0,'2026-05-03 17:00:25','2026-05-03 17:00:25',2046841090203357186,2046841090203357186);
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
INSERT INTO `problem_set_problem` VALUES (1,1,1,1,'','2026-04-29 15:14:10'),(2,3,2,1,NULL,'2026-05-03 17:00:25'),(3,3,1,2,NULL,'2026-05-03 17:00:25');
/*!40000 ALTER TABLE `problem_set_problem` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `problem_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_problem_tag` (`problem_id`,`tag_id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='题目标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `problem_tag` WRITE;
/*!40000 ALTER TABLE `problem_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `problem_tag` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签描述',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '#409EFF' COMMENT '标签颜色',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'test','test','#409EFF','2026-04-15 18:40:26','2026-04-15 18:40:26');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `test_case`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `input` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '输入数据',
  `output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预期输出',
  `is_sample` tinyint DEFAULT '0' COMMENT '是否为样例：0-否，1-是',
  `score` int DEFAULT '0' COMMENT '分值',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_is_sample` (`is_sample`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='测试用例表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `test_case` WRITE;
/*!40000 ALTER TABLE `test_case` DISABLE KEYS */;
INSERT INTO `test_case` VALUES (1,1,'1 2','3',0,0,0,0,'2026-04-15 18:00:25','2026-04-22 06:27:30'),(3,1,'4 5','9',1,10,1,0,'2026-04-22 06:27:53','2026-04-22 06:27:53');
/*!40000 ALTER TABLE `test_case` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


