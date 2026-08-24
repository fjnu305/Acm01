-- 模块 7：组队匹配
-- status 存储：0=队长手动关闭/解散标记；1=开放（招募中/已满由成员数实时计算）
-- 人数以 team_member.status=1 为准，不再冗余 current_count

CREATE TABLE IF NOT EXISTS `team_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '组队帖ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `description` TEXT NULL COMMENT '需求描述',
    `rating_min` INT NOT NULL DEFAULT 0 COMMENT '期望 Rating 下限',
    `rating_max` INT NOT NULL DEFAULT 4000 COMMENT '期望 Rating 上限',
    `region` VARCHAR(100) NULL DEFAULT NULL COMMENT '期望地区/学校',
    `tags` VARCHAR(500) NULL DEFAULT NULL COMMENT '擅长算法标签，逗号分隔',
    `member_limit` INT NOT NULL DEFAULT 3 COMMENT '队伍总人数（含队长）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0已关闭 1开放(招募/满员由成员数推导)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_status_time` (`status`, `created_time`),
    INDEX `idx_region` (`region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队帖';

CREATE TABLE IF NOT EXISTS `team_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员记录ID',
    `team_post_id` BIGINT NOT NULL COMMENT '组队帖ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'LEADER/MEMBER',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1已加入',
    `pending_kind` VARCHAR(16) NULL DEFAULT NULL COMMENT 'INVITE队长邀请 APPLY用户申请',
    `joined_time` DATETIME NULL DEFAULT NULL COMMENT '加入时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_team_user` (`team_post_id`, `user_id`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_team_post_status` (`team_post_id`, `status`, `pending_kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队成员';

CREATE TABLE IF NOT EXISTS `team_member_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
    `team_post_id` BIGINT NOT NULL COMMENT '组队帖ID',
    `user_id` BIGINT NOT NULL COMMENT '相关用户ID',
    `action` VARCHAR(32) NOT NULL COMMENT 'INVITE/APPLY/APPROVE/REJECT/ACCEPT/DECLINE/AUTO_REJECT_FULL',
    `actor_id` BIGINT NULL DEFAULT NULL COMMENT '操作人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_team_time` (`team_post_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队成员操作审计';

CREATE TABLE IF NOT EXISTS `match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '匹配记录ID',
    `team_post_id` BIGINT NOT NULL COMMENT '组队帖ID',
    `user_id` BIGINT NOT NULL COMMENT '被推荐用户ID',
    `match_score` DECIMAL(10, 4) NOT NULL COMMENT '匹配得分',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_team_score` (`team_post_id`, `match_score` DESC),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队匹配推荐记录';
