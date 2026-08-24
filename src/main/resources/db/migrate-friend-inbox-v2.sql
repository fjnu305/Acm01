-- 从初版 friend-inbox 表结构迁移到优化版（仅在已执行旧脚本时使用一次）
USE acm;

-- inbox_message: category + message_type + payload_json -> type + ref_type/ref_id
ALTER TABLE inbox_message
    ADD COLUMN type VARCHAR(48) NULL COMMENT '点分类型' AFTER sender_id,
    ADD COLUMN ref_type VARCHAR(32) NULL AFTER body,
    ADD COLUMN ref_id BIGINT NULL AFTER ref_type;

UPDATE inbox_message
SET type = CASE message_type
    WHEN 'SYSTEM_BROADCAST' THEN 'system.broadcast'
    WHEN 'FRIEND_REQUEST' THEN 'friend.request'
    WHEN 'FRIEND_ACCEPTED' THEN 'friend.accepted'
    WHEN 'FRIEND_REJECTED' THEN 'friend.rejected'
    WHEN 'PERSONAL' THEN 'personal.message'
    ELSE LOWER(CONCAT(IFNULL(category, 'system'), '.', IFNULL(message_type, 'unknown')))
END
WHERE type IS NULL;

UPDATE inbox_message
SET ref_type = 'friend_request',
    ref_id = CAST(JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.requestId')) AS UNSIGNED)
WHERE payload_json IS NOT NULL
  AND JSON_EXTRACT(payload_json, '$.requestId') IS NOT NULL
  AND ref_id IS NULL;

ALTER TABLE inbox_message
    MODIFY COLUMN type VARCHAR(48) NOT NULL,
    DROP COLUMN category,
    DROP COLUMN message_type,
    DROP COLUMN payload_json;

ALTER TABLE inbox_message
    ADD INDEX idx_type_prefix (type),
    ADD INDEX idx_ref (ref_type, ref_id);

-- friendship: 双向双行 -> 规范化用户对（需无数据或先清空）
-- 若已有 friendship 旧数据，请先备份；以下假设可重建
CREATE TABLE IF NOT EXISTS friendship_new (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_low_id     BIGINT       NOT NULL,
    user_high_id    BIGINT       NOT NULL,
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friend_pair (user_low_id, user_high_id),
    INDEX idx_user_low (user_low_id),
    INDEX idx_user_high (user_high_id),
    CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO friendship_new (user_low_id, user_high_id, created_time)
SELECT LEAST(user_id, friend_id), GREATEST(user_id, friend_id), MIN(created_time)
FROM friendship
GROUP BY LEAST(user_id, friend_id), GREATEST(user_id, friend_id);

DROP TABLE IF EXISTS friendship;
RENAME TABLE friendship_new TO friendship;
