package org.fjnu305.acm01.module.team.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MatchRecordEntity {

    private Long id;
    private Long teamPostId;
    private Long userId;
    private BigDecimal matchScore;
    private LocalDateTime createdTime;
}
