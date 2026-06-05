SET NAMES utf8mb4;

ALTER TABLE `submission_case_result`
  ADD COLUMN `is_sample` tinyint DEFAULT '0' COMMENT '是否样例：0-否，1-是' AFTER `error_message`,
  ADD COLUMN `input_preview` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '样例输入预览；隐藏用例为空' AFTER `is_sample`,
  ADD COLUMN `expected_output_preview` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '样例期望输出预览；隐藏用例为空' AFTER `input_preview`,
  ADD COLUMN `actual_output_preview` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '样例实际输出预览；隐藏用例为空' AFTER `expected_output_preview`,
  ADD COLUMN `output_diff_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '输出差异脱敏摘要' AFTER `actual_output_preview`;

CREATE TABLE IF NOT EXISTS `judge_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '反馈生成状态',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '反馈内容',
  `source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '反馈来源：LLM/STATIC_FALLBACK等',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型名称',
  `agent_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Agent 类型',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链路追踪ID',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Agent 错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_submission_id` (`submission_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_agent_type` (`agent_type`) USING BTREE,
  KEY `idx_trace_id` (`trace_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='判题智能反馈表';
