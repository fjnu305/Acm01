-- 迁移到「官方即系统好友」模型（在 v1/v2 脚本之后执行一次）
USE acm;

INSERT INTO `user` (username, password, nickname, status, deleted)
SELECT 'acmer_official',
       '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW',
       'ACMer 官方',
       1,
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'acmer_official');

SET @official_id = (SELECT id FROM `user` WHERE username = 'acmer_official' LIMIT 1);

-- sender_id 允许 NULL 的旧表：把系统消息改由官方账号发出
UPDATE inbox_message
SET sender_id = @official_id
WHERE sender_id IS NULL
  AND @official_id IS NOT NULL;

-- 去掉 type 列（若存在）
SET @has_type = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbox_message' AND COLUMN_NAME = 'type'
);
SET @sql_drop_type = IF(@has_type > 0, 'ALTER TABLE inbox_message DROP COLUMN type', 'SELECT 1');
PREPARE stmt FROM @sql_drop_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE inbox_message
    MODIFY COLUMN sender_id BIGINT NOT NULL COMMENT '发件人（含官方账号）';

-- 规范化 friendship（若仍是 user_id/friend_id 旧结构）
SET @has_old_friend = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'friendship' AND COLUMN_NAME = 'user_id'
);
SET @sql_migrate_friend = IF(@has_old_friend > 0,
    'CREATE TABLE IF NOT EXISTS friendship_new (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_low_id BIGINT NOT NULL,
        user_high_id BIGINT NOT NULL,
        created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY uk_friend_pair (user_low_id, user_high_id),
        INDEX idx_user_low (user_low_id),
        INDEX idx_user_high (user_high_id),
        CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    INSERT IGNORE INTO friendship_new (user_low_id, user_high_id, created_time)
    SELECT LEAST(user_id, friend_id), GREATEST(user_id, friend_id), MIN(created_time)
    FROM friendship GROUP BY LEAST(user_id, friend_id), GREATEST(user_id, friend_id);
    DROP TABLE friendship;
    RENAME TABLE friendship_new TO friendship;',
    'SELECT 1');
PREPARE stmt2 FROM @sql_migrate_friend;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

INSERT IGNORE INTO friendship (user_low_id, user_high_id)
SELECT LEAST(u.id, @official_id), GREATEST(u.id, @official_id)
FROM `user` u
WHERE u.username <> 'acmer_official'
  AND u.deleted = 0
  AND @official_id IS NOT NULL;
