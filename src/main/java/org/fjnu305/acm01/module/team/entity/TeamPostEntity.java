package org.fjnu305.acm01.module.team.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamPostEntity {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer ratingMin;
    private Integer ratingMax;
    private String region;
    private String tags;
    private Integer memberLimit;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
