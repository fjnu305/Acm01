package org.fjnu305.acm01.module.contest.log.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CrawlLogVO {

    private Long id;
    private String source;
    private String status;
    private String triggerType;
    private String errorType;
    private Integer httpStatus;
    private String requestUrl;
    private Integer fetchedCount;
    private Integer insertedCount;
    private Integer updatedCount;
    private Integer skippedCount;
    private Integer ignoredCount;
    private Integer elapsedMs;
    private String errorMessage;
    private LocalDateTime createdTime;
}
