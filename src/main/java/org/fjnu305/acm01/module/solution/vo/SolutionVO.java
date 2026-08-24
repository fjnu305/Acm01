package org.fjnu305.acm01.module.solution.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolutionVO {

    private Long id;
    private Long userId;
    private String authorName;
    private String title;
    private String problemSource;
    private String problemId;
    private String tags;
    private Integer favoriteCount;
    private Integer viewCount;
    private LocalDateTime createdTime;
    private Boolean favorited;
}
