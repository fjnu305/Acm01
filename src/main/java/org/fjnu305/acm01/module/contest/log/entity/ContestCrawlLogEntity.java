package org.fjnu305.acm01.module.contest.log.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContestCrawlLogEntity {

    private Long id;
    private String source;
    private String status;
    private String triggerType;
    private String errorType;
    private Integer httpStatus;
    private String requestUrl;
    private Integer maxRetries;
    private Integer totalAttempts;
    private Integer fetchedCount;
    private Integer insertedCount;
    private Integer updatedCount;
    private Integer skippedCount;
    private Integer ignoredCount;
    private Integer elapsedMs;
    private String errorMessage;
    private String errorDetail;
    private LocalDateTime createdTime;
}
