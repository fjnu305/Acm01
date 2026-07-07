-- 模块 2：统一赛事表 contest
USE acm;

CREATE TABLE IF NOT EXISTS contest (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '赛事ID',
    source          VARCHAR(32)  NOT NULL COMMENT '来源，对应 ContestSource 枚举',
    external_id     VARCHAR(128) NOT NULL COMMENT '平台原始ID',
    title           VARCHAR(255) NOT NULL COMMENT '赛事名称',
    description     TEXT COMMENT '赛事描述',
    url             VARCHAR(500) COMMENT '报名/详情链接',
    start_time      DATETIME     NOT NULL COMMENT '开始时间',
    end_time        DATETIME COMMENT '结束时间',
    register_start  DATETIME COMMENT '报名开始',
    register_end    DATETIME COMMENT '报名截止',
    status          TINYINT DEFAULT 1 COMMENT '1即将开始 2进行中 3已结束',
    difficulty      VARCHAR(32) COMMENT '难度标签',
    contest_type    VARCHAR(32) COMMENT 'ICPC/OI/个人赛等',
    location        VARCHAR(100) COMMENT '线上/线下地点',
    raw_hash        VARCHAR(64) COMMENT '原始数据哈希，用于增量判断',
    last_crawled_at DATETIME COMMENT '最后爬取时间',
    created_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_source_external (source, external_id),
    INDEX idx_start_time (start_time),
    INDEX idx_source (source),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一赛事表';
