package org.fjnu305.acm01.module.sync.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RatingSnapshotVO {

    private String platform;
    private Integer rating;
    private Integer maxRating;
    private String rank;
    private LocalDate snapshotDate;
}
