package org.fjnu305.acm01.module.team;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.service.TeamApplicationService;
import org.fjnu305.acm01.module.team.service.TeamInvitationService;
import org.fjnu305.acm01.module.team.service.TeamPostCommandService;
import org.fjnu305.acm01.module.team.service.TeamQueryService;
import org.fjnu305.acm01.module.team.service.TeamService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 门面层委托与水平越权。
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceAccessTest {

    @Mock
    private TeamPostCommandService commandService;
    @Mock
    private TeamQueryService queryService;
    @Mock
    private TeamInvitationService invitationService;
    @Mock
    private TeamApplicationService applicationService;

    @InjectMocks
    private TeamService teamService;

    @Test
    void invite_delegatesToInvitationService() {
        teamService.invite(1L, SecurityTestFixtures.USER_B_ID, 99L);
        verify(invitationService).invite(1L, SecurityTestFixtures.USER_B_ID, 99L);
    }

    @Test
    void invite_nonLeader_propagatesForbidden() {
        doThrow(new BusinessException(ErrorCode.TEAM_FORBIDDEN))
                .when(invitationService).invite(1L, SecurityTestFixtures.USER_A_ID, 99L);

        assertThatThrownBy(() ->
                teamService.invite(1L, SecurityTestFixtures.USER_A_ID, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FORBIDDEN.getCode());
    }

    @Test
    void acceptInvite_delegatesToInvitationService() {
        teamService.acceptInvite(5L, SecurityTestFixtures.USER_A_ID);
        verify(invitationService).acceptInvite(5L, SecurityTestFixtures.USER_A_ID);
    }

    @Test
    void apply_delegatesToApplicationService() {
        teamService.apply(1L, SecurityTestFixtures.USER_A_ID);
        verify(applicationService).apply(1L, SecurityTestFixtures.USER_A_ID);
    }
}
