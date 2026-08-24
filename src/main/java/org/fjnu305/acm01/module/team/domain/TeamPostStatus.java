package org.fjnu305.acm01.module.team.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamPostStatus {

    CLOSED(0),
    RECRUITING(1),
    FULL(2);

    private final int code;

    public static TeamPostStatus fromStored(int storedStatus, int acceptedCount, int memberLimit) {
        if (storedStatus == CLOSED.code) {
            return CLOSED;
        }
        if (acceptedCount >= memberLimit) {
            return FULL;
        }
        return RECRUITING;
    }

    public static int toStored(TeamPostStatus effective, int manualStoredStatus) {
        if (manualStoredStatus == CLOSED.code) {
            return CLOSED.code;
        }
        return effective == FULL ? FULL.code : RECRUITING.code;
    }
}
