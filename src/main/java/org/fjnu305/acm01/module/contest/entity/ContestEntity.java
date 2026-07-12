package org.fjnu305.acm01.module.contest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 */
@Data
public class ContestEntity {

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

    private String rawHash;

    private LocalDateTime lastCrawledAt;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private Integer deleted;
}
