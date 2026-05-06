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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

