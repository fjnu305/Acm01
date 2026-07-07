package org.fjnu305.acm01.module.contest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫源配置表 {@code contest_source} 实体。
 * <p>表结构见 {@code docs/module-2-contest-crawler.md §3.2}，{@code source_code} 与 {@link org.fjnu305.acm01.Common.enums.ContestSource} 对齐。</p>
 */
@Data
public class ContestSourceEntity {

    /** 主键 id BIGINT AUTO_INCREMENT */
    private Long id;

    /** 平台编码唯一，如 codeforces，列 source_code VARCHAR(32) UNIQUE */
    private String sourceCode;

    /** 平台显示名，列 source_name VARCHAR(64) */
    private String sourceName;

    /** 是否启用爬虫 1是 0否，列 crawl_enabled TINYINT DEFAULT 1 */
    private Integer crawlEnabled;

    /** Quartz cron 表达式，列 crawl_cron VARCHAR(64) */
    private String crawlCron;

    /** 两次请求最小间隔（秒），列 rate_limit_sec INT DEFAULT 3 */
    private Integer rateLimitSec;

    /** 上次爬取时间，列 last_crawl_time DATETIME */
    private LocalDateTime lastCrawlTime;

    /** 上次爬取结果 SUCCESS/FAILED，列 last_crawl_status VARCHAR(32) */
    private String lastCrawlStatus;

    /** 连续失败次数，列 fail_count INT DEFAULT 0 */
    private Integer failCount;

    /** 创建时间，列 created_time DATETIME DEFAULT CURRENT_TIMESTAMP */
    private LocalDateTime createdTime;
}
