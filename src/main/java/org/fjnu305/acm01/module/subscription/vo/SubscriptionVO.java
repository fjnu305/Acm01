package org.fjnu305.acm01.module.subscription.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionVO {

    private Long id;
    private Long contestId;
    private String contestTitle;
    private String source;
    private LocalDateTime contestStartTime;
    private Integer remindBeforeMinutes;
    private String channel;
    private Integer status;
    private LocalDateTime createdTime;
}
