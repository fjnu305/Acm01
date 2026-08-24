package org.fjnu305.acm01.module.team.domain;

import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamPostPolicyTest {

    @Test
    void effectiveStatus_closedPost_staysClosed() {
        TeamPostEntity post = post(0, 3);
        assertThat(TeamPostPolicy.effectiveStatus(post, 1)).isEqualTo(TeamPostStatus.CLOSED);
    }

    @Test
    void effectiveStatus_openAndNotFull_isRecruiting() {
        TeamPostEntity post = post(1, 3);
        assertThat(TeamPostPolicy.effectiveStatus(post, 2)).isEqualTo(TeamPostStatus.RECRUITING);
    }

    @Test
    void effectiveStatus_openAndFull_isFull() {
        TeamPostEntity post = post(1, 3);
        assertThat(TeamPostPolicy.effectiveStatus(post, 3)).isEqualTo(TeamPostStatus.FULL);
    }

    @Test
    void canRecruit_whenClosed_returnsFalse() {
        TeamPostEntity post = post(0, 3);
        assertThat(TeamPostPolicy.canRecruit(post, 1)).isFalse();
    }

    @Test
    void canRecruit_whenFull_returnsFalse() {
        TeamPostEntity post = post(1, 3);
        assertThat(TeamPostPolicy.canRecruit(post, 3)).isFalse();
    }

    private static TeamPostEntity post(int status, int limit) {
        TeamPostEntity post = new TeamPostEntity();
        post.setStatus(status);
        post.setMemberLimit(limit);
        return post;
    }
}
