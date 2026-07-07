package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛事 API 返回对象。
 */
@Data
@Builder
public class ContestVO {

    private Long id;
    private String source;
    private String externalId;
    private String title;
    private String url;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String difficulty;
    private String contestType;
    private String location;
}
