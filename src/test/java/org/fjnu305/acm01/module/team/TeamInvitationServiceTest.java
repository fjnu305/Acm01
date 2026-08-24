package org.fjnu305.acm01.module.team;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.service.TeamAccessGuard;
import org.fjnu305.acm01.module.team.service.TeamInvitationService;
import org.fjnu305.acm01.module.team.service.TeamMemberLifecycleService;
import org.fjnu305.acm01.module.team.service.TeamNotificationService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamInvitationServiceTest {

    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private TeamAccessGuard teamAccessGuard;
    @Mock
    private TeamMemberLifecycleService lifecycleService;
    @Mock
    private TeamNotificationService teamNotificationService;
    @Mock
    private FriendService friendService;

    @InjectMocks
    private TeamInvitationService invitationService;

    @Test
    void invite_nonLeader_throwsForbidden() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        doThrow(new BusinessException(ErrorCode.TEAM_FORBIDDEN))
                .when(teamAccessGuard).requireLeader(post, SecurityTestFixtures.USER_A_ID);

        assertThatThrownBy(() ->
                invitationService.invite(1L, SecurityTestFixtures.USER_A_ID, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FORBIDDEN.getCode());
    }

    @Test
    void acceptInvite_applyRecord_throwsForbidden() {
        TeamMemberEntity member = new TeamMemberEntity();
        member.setTeamPostId(1L);
        member.setUserId(SecurityTestFixtures.USER_A_ID);
        member.setStatus(0);
        member.setPendingKind(PendingKind.APPLY);
        when(teamAccessGuard.requirePendingInvite(5L, SecurityTestFixtures.USER_A_ID))
                .thenThrow(new BusinessException(ErrorCode.TEAM_FORBIDDEN, "Wait for leader approval"));

        assertThatThrownBy(() ->
                invitationService.acceptInvite(5L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FORBIDDEN.getCode());
    }

    @Test
    void acceptInvite_wrongUser_throwsInviteNotFound() {
        when(teamAccessGuard.requirePendingInvite(5L, SecurityTestFixtures.USER_A_ID))
                .thenThrow(new BusinessException(ErrorCode.TEAM_INVITE_NOT_FOUND));

        assertThatThrownBy(() ->
                invitationService.acceptInvite(5L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_INVITE_NOT_FOUND.getCode());
    }

    private static TeamPostEntity recruitingPost(Long leaderId) {
        TeamPostEntity post = new TeamPostEntity();
        post.setId(1L);
        post.setUserId(leaderId);
        post.setStatus(1);
        post.setMemberLimit(3);
        return post;
    }
}
