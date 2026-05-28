USE `emiya_oj_problem`;

ALTER TABLE `test_case`
  MODIFY COLUMN `input` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输入数据',
  MODIFY COLUMN `output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预期输出';
