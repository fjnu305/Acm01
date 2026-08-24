package org.fjnu305.acm01.module.solution.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolutionFavoriteEntity {

    private Long userId;
    private Long solutionId;
    private LocalDateTime createdTime;
}
