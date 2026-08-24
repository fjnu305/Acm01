package org.fjnu305.acm01.module.team.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamCapacityTest {

    @Test
    void hasSlot_whenBelowLimit_returnsTrue() {
        assertThat(TeamCapacity.hasSlot(1, 3)).isTrue();
        assertThat(TeamCapacity.hasSlot(2, 3)).isTrue();
    }

    @Test
    void hasSlot_whenAtLimit_returnsFalse() {
        assertThat(TeamCapacity.hasSlot(3, 3)).isFalse();
    }

    @Test
    void isFull_whenAtOrAboveLimit_returnsTrue() {
        assertThat(TeamCapacity.isFull(3, 3)).isTrue();
        assertThat(TeamCapacity.isFull(4, 3)).isTrue();
    }
}
