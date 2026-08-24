-- 模块 1：用户表
USE acm;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL（本地上传为 /uploads/avatars/...）',
    `gender` TINYINT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `school` VARCHAR(100) NULL DEFAULT NULL COMMENT '学校',
    `bio` VARCHAR(500) NULL DEFAULT NULL COMMENT '个人简介',
    `cf_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Codeforces账号',
    `atcoder_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT 'AtCoder账号',
    `nowcoder_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT '牛客账号',
    `luogu_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT '洛谷账号',
    `cf_rating` INT NULL DEFAULT 0 COMMENT 'Codeforces Rating',
    `solved_count` INT NULL DEFAULT 0 COMMENT '总刷题数',
    `ac_count` INT NULL DEFAULT 0 COMMENT 'AC题目数',
    `contest_count` INT NULL DEFAULT 0 COMMENT '参赛场次',
    `status` TINYINT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
    `created_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `username` (`username`) USING BTREE,
    INDEX `idx_cf_rating` (`cf_rating`) USING BTREE,
    INDEX `idx_solved_count` (`solved_count`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='用户表';
