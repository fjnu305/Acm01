package org.fjnu305.acm01.module.sync.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CfRatingChangeVO {

    private Integer contestId;
    private String contestName;
    private Integer rank;
    private Integer oldRating;
    private Integer newRating;
    private LocalDateTime ratedAt;
}
