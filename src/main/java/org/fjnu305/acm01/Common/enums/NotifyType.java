package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotifyType {

    EMAIL("email"),
    IN_APP("in_app"),
    WEBSOCKET("websocket");

    private final String value;
}
