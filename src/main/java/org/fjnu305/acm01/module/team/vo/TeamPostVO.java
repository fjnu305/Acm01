package org.fjnu305.acm01.module.team.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamPostVO {

    private Long id;
    private Long userId;
    private String authorName;
    private String title;
    private String description;
    private Integer ratingMin;
    private Integer ratingMax;
    private String region;
    private String tags;
    private Integer memberLimit;
    private Integer currentCount;
    private Integer status;
    private LocalDateTime createdTime;
    private List<TeamMemberPreviewVO> memberPreview;
}
