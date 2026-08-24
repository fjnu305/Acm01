package org.fjnu305.acm01.module.team.domain;

public final class TeamCapacity {

    private TeamCapacity() {
    }

    public static boolean isFull(int acceptedCount, int memberLimit) {
        return acceptedCount >= memberLimit;
    }

    public static boolean hasSlot(int acceptedCount, int memberLimit) {
        return acceptedCount < memberLimit;
    }
}
