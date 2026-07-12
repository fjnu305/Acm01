package org.fjnu305.acm01.module.subscription.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubscribeRequest {

    @NotNull
    private Long contestId;

    @NotEmpty
    private List<Integer> remindBeforeMinutes;
}
