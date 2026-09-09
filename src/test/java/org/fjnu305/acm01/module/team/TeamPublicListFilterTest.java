package org.fjnu305.acm01.module.team;

import org.fjnu305.acm01.Common.access.Viewer;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.team.service.TeamQueryService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamPublicListFilterTest {

    @Test
    void anonymousCannotRequestDraftStatus() {
        assertThat(TeamQueryService.publicStatusFilter(0, Viewer.anonymous())).isEqualTo(1);
        assertThat(TeamQueryService.publicStatusFilter(null, Viewer.anonymous())).isEqualTo(1);
        assertThat(TeamQueryService.publicStatusFilter(2, Viewer.anonymous())).isEqualTo(2);
    }

    @Test
    void adminCanRequestDraftStatus() {
        Viewer admin = Viewer.of(new LoginUser(1L, "admin", "ADMIN"));
        assertThat(TeamQueryService.publicStatusFilter(0, admin)).isEqualTo(0);
    }
}
