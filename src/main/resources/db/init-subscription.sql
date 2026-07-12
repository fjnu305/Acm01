-- 模块 3：赛事订阅与提醒（仅邮件渠道）
USE acm;

CREATE TABLE IF NOT EXISTS contest_subscription (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT       NOT NULL COMMENT '用户ID',
    contest_id            BIGINT       NOT NULL COMMENT '赛事ID contest.id',
    remind_before_minutes INT          NOT NULL COMMENT '赛前提醒分钟数，如1440=24h,60=1h',
    channel               VARCHAR(16)  NOT NULL DEFAULT 'EMAIL' COMMENT '固定EMAIL',
    status                TINYINT      NOT NULL DEFAULT 1 COMMENT '1生效 0已取消',
    created_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_contest_remind (user_id, contest_id, remind_before_minutes),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户赛事订阅';

CREATE TABLE IF NOT EXISTS notify_task (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id  BIGINT       NOT NULL COMMENT '来源订阅ID',
    channel          VARCHAR(16)  NOT NULL DEFAULT 'EMAIL' COMMENT '固定EMAIL',
    scheduled_at     DATETIME     NOT NULL COMMENT '计划发送时刻',
    status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/FAILED/CANCELLED',
    sent_at          DATETIME     NULL,
    retry_count      INT          NOT NULL DEFAULT 0,
    error_message    VARCHAR(500) NULL,
    idempotent_key   VARCHAR(128) NOT NULL COMMENT '幂等键',
    created_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotent (idempotent_key),
    INDEX idx_scan (status, scheduled_at),
    INDEX idx_subscription (subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待执行提醒任务';

CREATE TABLE IF NOT EXISTS notify_log (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    notify_task_id   BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    channel          VARCHAR(16)  NOT NULL DEFAULT 'EMAIL',
    target           VARCHAR(255) NULL COMMENT '邮箱等',
    status           VARCHAR(16)  NOT NULL COMMENT 'SUCCESS/FAILED',
    provider_msg_id  VARCHAR(128) NULL,
    error_message    VARCHAR(500) NULL,
    created_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (notify_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送日志';
