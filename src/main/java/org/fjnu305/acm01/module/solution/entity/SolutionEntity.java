package org.fjnu305.acm01.module.solution.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolutionEntity {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String problemSource;
    private String problemId;
    private String tags;
    private Integer status;
    private Integer favoriteCount;
    private Integer viewCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
