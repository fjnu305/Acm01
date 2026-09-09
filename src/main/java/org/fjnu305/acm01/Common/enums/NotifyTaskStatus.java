package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotifyTaskStatus {

    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    SENT("SENT"),
    FAILED("FAILED"),
    DEAD("DEAD"),
    CANCELLED("CANCELLED");

    private final String value;
}
