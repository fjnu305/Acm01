package org.fjnu305.acm01.module.team.domain;

import org.fjnu305.acm01.module.team.entity.TeamPostEntity;

public final class TeamPostPolicy {

    private TeamPostPolicy() {
    }

    public static boolean isManuallyClosed(TeamPostEntity post) {
        return post.getStatus() != null && post.getStatus() == TeamPostStatus.CLOSED.getCode();
    }

    public static boolean canRecruit(TeamPostEntity post, int acceptedCount) {
        return !isManuallyClosed(post) && TeamCapacity.hasSlot(acceptedCount, post.getMemberLimit());
    }

    public static TeamPostStatus effectiveStatus(TeamPostEntity post, int acceptedCount) {
        int stored = post.getStatus() == null ? TeamPostStatus.RECRUITING.getCode() : post.getStatus();
        return TeamPostStatus.fromStored(stored, acceptedCount, post.getMemberLimit());
    }
}
