package org.fjnu305.acm01.module.team.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamPostStatusTest {

    @Test
    void fromStored_closedWins() {
        assertThat(TeamPostStatus.fromStored(0, 1, 3)).isEqualTo(TeamPostStatus.CLOSED);
    }

    @Test
    void fromStored_fullWhenAtCapacity() {
        assertThat(TeamPostStatus.fromStored(1, 3, 3)).isEqualTo(TeamPostStatus.FULL);
    }

    @Test
    void fromStored_recruitingWhenHasSlot() {
        assertThat(TeamPostStatus.fromStored(1, 2, 3)).isEqualTo(TeamPostStatus.RECRUITING);
    }

    @Test
    void toStored_preservesManualClose() {
        assertThat(TeamPostStatus.toStored(TeamPostStatus.RECRUITING, 0)).isEqualTo(0);
    }

    @Test
    void toStored_writesFullCode() {
        assertThat(TeamPostStatus.toStored(TeamPostStatus.FULL, 1)).isEqualTo(2);
    }
}
