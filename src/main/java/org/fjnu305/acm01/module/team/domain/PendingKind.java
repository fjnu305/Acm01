package org.fjnu305.acm01.module.team.domain;

public final class PendingKind {

    public static final String INVITE = "INVITE";
    public static final String APPLY = "APPLY";

    private PendingKind() {
    }

    public static boolean isInvite(String kind) {
        return INVITE.equals(kind);
    }

    public static boolean isApply(String kind) {
        return APPLY.equals(kind);
    }
}
