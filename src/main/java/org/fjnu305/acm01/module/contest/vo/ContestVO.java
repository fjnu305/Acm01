package org.fjnu305.acm01.module.contest.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * èµ›äº‹ API è¿”å›žå¯¹è±¡ã€? */
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
