package org.fjnu305.acm01.module.contest.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContestDetailVO {

    private Long id;
    private String source;
    private String externalId;
    private String title;
    private String description;
    private String url;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registerStart;
    private LocalDateTime registerEnd;
    private Integer status;
    private String difficulty;
    private String contestType;
    private String location;
    private LocalDateTime lastCrawledAt;
}
