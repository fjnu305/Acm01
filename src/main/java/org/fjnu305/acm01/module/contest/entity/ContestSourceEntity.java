package org.fjnu305.acm01.module.contest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫源配置表 {@code contest_source} 实体?
 */
@Data
public class ContestSourceEntity {

    /** 主键 id BIGINT AUTO_INCREMENT */
    private Long id;

    private String sourceCode;

    private String sourceName;

    private Integer crawlEnabled;

    private String crawlCron;

    private Integer rateLimitSec;

    private LocalDateTime lastCrawlTime;

    private String lastCrawlStatus;

    private Integer failCount;

    private LocalDateTime createdTime;
}
