-- 模块 6：社交社区
USE acm;

CREATE TABLE IF NOT EXISTS topic (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(64)  NOT NULL COMMENT '话题名',
    description     VARCHAR(255) NULL,
    post_count      INT          NOT NULL DEFAULT 0,
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题';

CREATE TABLE IF NOT EXISTS post (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '作者ID',
    content         TEXT         NOT NULL COMMENT '正文',
    topic_id        BIGINT       NULL COMMENT '话题ID',
    like_count      INT          NOT NULL DEFAULT 0,
    comment_count   INT          NOT NULL DEFAULT 0,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 已删除',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_topic (topic_id),
    INDEX idx_status_time (status, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态';

CREATE TABLE IF NOT EXISTS comment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id         BIGINT       NOT NULL COMMENT '动态ID',
    user_id         BIGINT       NOT NULL COMMENT '评论者ID',
    content         VARCHAR(1000) NOT NULL,
    parent_id       BIGINT       NULL COMMENT '父评论ID',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 已删除',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论';

CREATE TABLE IF NOT EXISTS post_like (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id         BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞';

CREATE TABLE IF NOT EXISTS follow (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id     BIGINT       NOT NULL COMMENT '关注者',
    followee_id     BIGINT       NOT NULL COMMENT '被关注者',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, followee_id),
    INDEX idx_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系';

INSERT IGNORE INTO topic (name, description) VALUES
('日常刷题', '刷题打卡与解题心得'),
('竞赛复盘', '赛后总结与经验分享'),
('求助答疑', '题目讨论与求助');
