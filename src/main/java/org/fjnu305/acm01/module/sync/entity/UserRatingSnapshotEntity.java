package org.fjnu305.acm01.module.sync.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserRatingSnapshotEntity {

    private Long id;
    private Long userId;
    private String platform;
    private Integer rating;
    private Integer maxRating;
    private String rank;
    private LocalDate snapshotDate;
    private LocalDateTime createdTime;
}
