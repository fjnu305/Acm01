package org.fjnu305.acm01.module.sync.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CfRatingHistoryEntity {

    private Long id;
    private Long userId;
    private Integer contestId;
    private String contestName;
    private Integer rank;
    private Integer oldRating;
    private Integer newRating;
    private LocalDateTime ratedAt;
}
