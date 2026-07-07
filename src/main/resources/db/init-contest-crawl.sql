-- 模块 2：赛事爬虫相关表（contest_source 配置 + contest_crawl_log 执行日志）
USE acm;

-- 爬虫源配置（与 ContestSource 枚举 value 对齐）
CREATE TABLE IF NOT EXISTS contest_source (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_code       VARCHAR(32)  NOT NULL UNIQUE COMMENT '对应 ContestSource.value',
    source_name       VARCHAR(64)  NOT NULL COMMENT '平台显示名',
    crawl_enabled     TINYINT      DEFAULT 1 COMMENT '是否启用 1是 0否',
    crawl_cron        VARCHAR(64)  DEFAULT '0 0 */6 * * ?' COMMENT 'Quartz cron',
    rate_limit_sec    INT          DEFAULT 3 COMMENT '两次请求最小间隔（秒）',
    last_crawl_time   DATETIME     NULL COMMENT '上次爬取时间',
    last_crawl_status VARCHAR(32)  NULL COMMENT 'SUCCESS / FAILED',
    fail_count        INT          DEFAULT 0 COMMENT '连续失败次数',
    created_time      DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫源配置表';

-- 爬虫执行日志（全平台统一字段）
CREATE TABLE IF NOT EXISTS contest_crawl_log (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    source           VARCHAR(32)  NOT NULL COMMENT '平台 ContestSource.value',
    status           VARCHAR(16)  NOT NULL COMMENT 'SUCCESS / FAILED',
    trigger_type     VARCHAR(16)  NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO=定时 MANUAL=手动',
    error_type       VARCHAR(32)  NULL COMMENT 'HTTP_ERROR/API_ERROR/PARSE_ERROR/TIMEOUT/UNKNOWN',
    http_status      INT          NULL COMMENT '最后一次 HTTP 状态码',
    request_url      VARCHAR(500) NULL COMMENT '主请求 URL',
    max_retries      TINYINT      NOT NULL DEFAULT 3 COMMENT '配置的最大重试次数',
    total_attempts   TINYINT      NOT NULL DEFAULT 1 COMMENT '实际尝试次数（含首次）',
    fetched_count    INT          NOT NULL DEFAULT 0 COMMENT '成功解析条数，失败为0',
    elapsed_ms       INT          NULL COMMENT '本次耗时毫秒',
    error_message    VARCHAR(1000) NULL COMMENT '错误摘要',
    error_detail     TEXT         NULL COMMENT '堆栈或响应片段',
    created_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source_time (source, created_time),
    INDEX idx_status_time (status, created_time),
    INDEX idx_error_type (error_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫执行日志';

INSERT IGNORE INTO contest_source (source_code, source_name, crawl_enabled, rate_limit_sec) VALUES
('codeforces', 'Codeforces', 1, 2),
('atcoder',    'AtCoder',    1, 3),
('nowcoder',   '牛客网',      1, 3),
('luogu',      '洛谷',        1, 3),
('ccpc',       'CCPC',       1, 5),
('icpc',       'ICPC',       1, 5),
('lanqiao',    '蓝桥杯',      1, 5);
