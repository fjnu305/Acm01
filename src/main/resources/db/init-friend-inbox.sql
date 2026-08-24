-- 好友 + 站内消息（与 notify 邮件提醒分离）
-- 核心抽象：官方账号是每个用户必须拥有的「系统好友」，所有消息都是好友私信。
--   - 官方公告 = 官方账号发给用户的私信
--   - 好友聊天 = 普通好友互发私信
--   - 好友申请通知 = 申请人发给被申请人的私信（ref 指向 friend_request 流程表）
-- friend_request 只管流程状态，正文在 inbox_message。
USE acm;

-- 官方账号（不可登录，仅作为消息发送方）
INSERT INTO `user` (username, password, nickname, status, deleted)
SELECT 'acmer_official',
       '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW',
       'ACMer 官方',
       1,
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'acmer_official');

CREATE TABLE IF NOT EXISTS friend_request (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id    BIGINT       NOT NULL COMMENT '申请人',
    addressee_id    BIGINT       NOT NULL COMMENT '被申请人',
    message         VARCHAR(200) NULL COMMENT '附言',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待处理 1已同意 2已拒绝 3已取消',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_request_pair (requester_id, addressee_id),
    INDEX idx_addressee_pending (addressee_id, status, created_time),
    INDEX idx_requester_pending (requester_id, status, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请（流程表）';

CREATE TABLE IF NOT EXISTS friendship (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_low_id     BIGINT       NOT NULL COMMENT '较小 user_id',
    user_high_id    BIGINT       NOT NULL COMMENT '较大 user_id',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friend_pair (user_low_id, user_high_id),
    INDEX idx_user_low (user_low_id),
    INDEX idx_user_high (user_high_id),
    CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系（含官方系统好友，每对用户一行）';

CREATE TABLE IF NOT EXISTS inbox_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id    BIGINT       NOT NULL COMMENT '收件人',
    sender_id       BIGINT       NOT NULL COMMENT '发件人（含官方账号）',
    title           VARCHAR(200) NOT NULL,
    body            TEXT         NOT NULL,
    ref_type        VARCHAR(32)  NULL COMMENT '可交互引用：friend_request / friend_accepted',
    ref_id          BIGINT       NULL COMMENT '引用实体 ID',
    read_at         DATETIME     NULL,
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recipient_time (recipient_id, created_time DESC),
    INDEX idx_recipient_unread (recipient_id, read_at),
    INDEX idx_sender (sender_id),
    INDEX idx_ref (ref_type, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友私信（官方通知亦走此表）';

-- 为已有用户补建与官方账号的好友关系
INSERT IGNORE INTO friendship (user_low_id, user_high_id)
SELECT LEAST(u.id, o.id), GREATEST(u.id, o.id)
FROM `user` u
         CROSS JOIN (SELECT id FROM `user` WHERE username = 'acmer_official' LIMIT 1) o
WHERE u.username <> 'acmer_official'
  AND u.deleted = 0;
