package org.fjnu305.acm01.module.notify.writeTask.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotifyScheduleCommand {

    private Long subscriptionId;
    private Long userId;
    private Long contestId;
    private String channel;
    private int remindBeforeMinutes;
    private LocalDateTime contestStartTime;
}
