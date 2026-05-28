SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `language` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='编程语言配置表';

CREATE TABLE IF NOT EXISTS `problem` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='题目表';

CREATE TABLE IF NOT EXISTS `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签描述',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '#409EFF' COMMENT '标签颜色',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标签表';

CREATE TABLE IF NOT EXISTS `problem_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_problem_tag` (`problem_id`,`tag_id`) USING BTREE,
  KEY `idx_problem_id` (`problem_id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='题目标签关联表';

CREATE TABLE IF NOT EXISTS `test_case` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='测试用例表';

CREATE TABLE IF NOT EXISTS `contest` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest';

CREATE TABLE IF NOT EXISTS `contest_problem` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest problem';

CREATE TABLE IF NOT EXISTS `contest_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'registration id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'register time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_contest_user` (`contest_id`,`user_id`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest registration';

CREATE TABLE IF NOT EXISTS `contest_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'admin id',
  `contest_id` bigint NOT NULL COMMENT 'contest id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `create_by` bigint DEFAULT NULL COMMENT 'operator id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_contest_admin` (`contest_id`,`user_id`) USING BTREE,
  KEY `idx_contest_id` (`contest_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='contest admin';

CREATE TABLE IF NOT EXISTS `problem_set` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='problem set';

CREATE TABLE IF NOT EXISTS `problem_set_problem` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='problem set problem relation';
