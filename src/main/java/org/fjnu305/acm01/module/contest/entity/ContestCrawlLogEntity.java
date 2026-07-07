package org.fjnu305.acm01.module.contest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫执行日志表 {@code contest_crawl_log} 实体。
 * <p>字段与全平台 {@link org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException} 及
 * {@link org.fjnu305.acm01.module.contest.crawl.service.CrawlLogService} 写入逻辑统一对应。</p>
 */
@Data
public class ContestCrawlLogEntity {

    private Long id;
    /** 对应 {@link org.fjnu305.acm01.Common.enums.ContestSource#getValue()} */
    private String source;
    /** SUCCESS / FAILED，见 {@link org.fjnu305.acm01.Common.enums.CrawlLogStatus} */
    private String status;
    /** AUTO / MANUAL，见 {@link org.fjnu305.acm01.Common.enums.CrawlTriggerType} */
    private String triggerType;
    /** 失败时填写，见 {@link org.fjnu305.acm01.Common.enums.CrawlErrorType} */
    private String errorType;
    private Integer httpStatus;
    private String requestUrl;
    private Integer maxRetries;
    private Integer totalAttempts;
    private Integer fetchedCount;
    private Integer elapsedMs;
    private String errorMessage;
    private String errorDetail;
    private LocalDateTime createdTime;
}
