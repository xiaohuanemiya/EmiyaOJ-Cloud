-- =====================================================
-- EmiyaOJ 编程语言配置 (emiya_oj_problem.language)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `emiya_oj_problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `emiya_oj_problem`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `language`;
CREATE TABLE `language` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '语言ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言名称，如 C++、Java、Python3',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示版本，如 C++20、Java 21',
  `language_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '命令模板中的语言版本值，如 c++20、c11',
  `compile_file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'main' COMMENT '源文件基础名，不含扩展名',
  `source_file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源文件扩展名，不含点',
  `executable_file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'main' COMMENT '运行命令中的可执行目标名',
  `compiled_file_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编译产物文件名，多个用英文逗号分隔；为空时使用 executable_file_name',
  `compile_command` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '编译命令模板，支持 {LanguageVersion}/{CompileFileName}/{SourceFileName}/{ExecutableFileName}',
  `run_command` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运行命令模板，支持 {LanguageVersion}/{CompileFileName}/{SourceFileName}/{ExecutableFileName}',
  `env_vars` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'GoJudge 环境变量，逗号或换行分隔',
  `is_compiled` tinyint NULL DEFAULT 1 COMMENT '是否需要编译：0-否，1-是',
  `time_limit_multiplier` decimal(5, 2) NULL DEFAULT 1.00 COMMENT '运行 CPU 时间限制倍数',
  `memory_limit_multiplier` decimal(5, 2) NULL DEFAULT 1.00 COMMENT '运行内存限制倍数',
  `compile_time_limit` int NULL DEFAULT 10000 COMMENT '编译 CPU 时间限制（毫秒）',
  `compile_memory_limit` int NULL DEFAULT 512 COMMENT '编译内存限制（MB）',
  `compile_proc_limit` int NULL DEFAULT 50 COMMENT '编译进程数限制',
  `run_proc_limit` int NULL DEFAULT 1 COMMENT '运行进程数限制',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name_version` (`name` ASC, `version` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '编程语言配置表' ROW_FORMAT = Dynamic;

INSERT INTO `language`
(`id`, `name`, `version`, `language_version`, `compile_file_name`, `source_file_ext`, `executable_file_name`, `compiled_file_names`, `compile_command`, `run_command`, `env_vars`, `is_compiled`, `time_limit_multiplier`, `memory_limit_multiplier`, `compile_time_limit`, `compile_memory_limit`, `compile_proc_limit`, `run_proc_limit`, `status`)
VALUES
(1, 'C++', 'C++20', 'c++20', 'main', 'cpp', 'main', 'main',
 '/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp',
 './{ExecutableFileName}',
 'PATH=/usr/bin:/bin', 1, 1.00, 1.00, 10000, 512, 50, 1, 1),
(2, 'C', 'C11', 'c11', 'main', 'c', 'main', 'main',
 '/usr/bin/gcc -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.c -lm',
 './{ExecutableFileName}',
 'PATH=/usr/bin:/bin', 1, 1.00, 1.00, 10000, 256, 50, 1, 1),
(3, 'Java', 'Java 21', '21', 'Main', 'java', 'Main', 'Main.class',
 '/usr/bin/javac {CompileFileName}.java',
 '/usr/bin/java {ExecutableFileName}',
 'PATH=/usr/bin:/bin', 1, 2.00, 2.00, 10000, 512, 50, 50, 1),
(4, 'Python3', 'Python 3.12', '3.12', 'main', 'py', 'main.py', NULL,
 NULL,
 '/usr/bin/python3 {SourceFileName}',
 'PATH=/usr/bin:/bin', 0, 3.00, 2.00, 10000, 256, 10, 1, 1),
(5, 'Go', 'Go 1.22', '1.22', 'main', 'go', 'main', 'main',
 '/usr/bin/go build -o {ExecutableFileName} {CompileFileName}.go',
 './{ExecutableFileName}',
 'PATH=/usr/bin:/bin,GOPATH=/tmp/go,GOCACHE=/tmp/go-cache', 1, 1.00, 1.00, 15000, 512, 50, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
