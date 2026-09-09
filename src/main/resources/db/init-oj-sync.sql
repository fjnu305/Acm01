-- 模块 10：OJ 竞技数据同步
USE acm;

CREATE TABLE IF NOT EXISTS oj_account (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT       NOT NULL COMMENT '用户ID',
    platform            VARCHAR(32)  NOT NULL COMMENT '平台 codeforces/atcoder/luogu/nowcoder',
    handle              VARCHAR(50)  NOT NULL COMMENT '平台账号',
    encrypted_credential VARCHAR(1024) NULL COMMENT 'AES-GCM 加密凭证',
    credential_iv       VARCHAR(64)  NULL COMMENT 'AES-GCM IV (Base64)',
    status              TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    last_sync_at        DATETIME     NULL COMMENT '上次同步时间',
    created_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_platform (user_id, platform),
    INDEX idx_platform_handle (platform, handle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OJ 账号绑定';

CREATE TABLE IF NOT EXISTS user_rating_snapshot (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    platform        VARCHAR(32)  NOT NULL COMMENT '平台',
    rating          INT          NOT NULL DEFAULT 0 COMMENT 'Rating',
    `max_rating`      INT          NULL COMMENT '历史最高 Rating',
    `rank`            VARCHAR(32)  NULL COMMENT '段位/排名',
    snapshot_date   DATE         NOT NULL COMMENT '快照日期',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_platform_date (user_id, platform, snapshot_date),
    INDEX idx_user_platform (user_id, platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户 Rating 日快照（Codeforces）';

CREATE TABLE IF NOT EXISTS `cf_rating_history` (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    contest_id   INT          NOT NULL COMMENT 'CF contestId',
    contest_name VARCHAR(255) NULL COMMENT '比赛名称',
    `rank`       INT          NULL COMMENT '当场名次',
    old_rating   INT          NOT NULL DEFAULT 0 COMMENT '赛前 Rating',
    new_rating   INT          NOT NULL DEFAULT 0 COMMENT '赛后 Rating',
    rated_at     DATETIME     NOT NULL COMMENT 'Rating 更新时间',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_contest (user_id, contest_id),
    INDEX idx_user_rated (user_id, rated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Codeforces 参赛 Rating 变化';
