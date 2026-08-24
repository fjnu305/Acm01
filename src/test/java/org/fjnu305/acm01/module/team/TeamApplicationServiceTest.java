package org.fjnu305.acm01.module.team;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.service.TeamAccessGuard;
import org.fjnu305.acm01.module.team.service.TeamApplicationService;
import org.fjnu305.acm01.module.team.service.TeamMemberLifecycleService;
import org.fjnu305.acm01.module.team.service.TeamNotificationService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamApplicationServiceTest {

    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private TeamAccessGuard teamAccessGuard;
    @Mock
    private TeamMemberLifecycleService lifecycleService;
    @Mock
    private TeamNotificationService teamNotificationService;

    @InjectMocks
    private TeamApplicationService applicationService;

    @Test
    void apply_ownTeam_throwsBadRequest() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_A_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);

        assertThatThrownBy(() -> applicationService.apply(1L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    }

    @Test
    void apply_alreadyApplied_throws() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        TeamMemberEntity existing = pendingApply(SecurityTestFixtures.USER_A_ID);
        when(teamMemberMapper.selectByTeamAndUser(1L, SecurityTestFixtures.USER_A_ID)).thenReturn(existing);

        assertThatThrownBy(() -> applicationService.apply(1L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_ALREADY_APPLIED.getCode());
    }

    @Test
    void apply_pendingInviteExists_throws() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        TeamMemberEntity existing = new TeamMemberEntity();
        existing.setPendingKind(PendingKind.INVITE);
        existing.setStatus(0);
        when(teamMemberMapper.selectByTeamAndUser(1L, SecurityTestFixtures.USER_A_ID)).thenReturn(existing);

        assertThatThrownBy(() -> applicationService.apply(1L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_PENDING_INVITE_EXISTS.getCode());
    }

    @Test
    void apply_whenFull_throws() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        when(teamMemberMapper.selectByTeamAndUser(1L, SecurityTestFixtures.USER_A_ID)).thenReturn(null);
        when(lifecycleService.countAccepted(1L)).thenReturn(3);

        assertThatThrownBy(() -> applicationService.apply(1L, SecurityTestFixtures.USER_A_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FULL.getCode());
    }

    @Test
    void apply_success_notifiesLeader() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        when(teamMemberMapper.selectByTeamAndUser(1L, SecurityTestFixtures.USER_A_ID)).thenReturn(null);
        when(lifecycleService.countAccepted(1L)).thenReturn(1);

        applicationService.apply(1L, SecurityTestFixtures.USER_A_ID);

        verify(teamMemberMapper).insert(any(TeamMemberEntity.class));
        verify(teamNotificationService).notifyApplicationSubmitted(
                eq(SecurityTestFixtures.USER_A_ID),
                eq(SecurityTestFixtures.USER_B_ID),
                eq(post),
                any());
    }

    @Test
    void approve_nonLeader_throwsForbidden() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        doThrow(new BusinessException(ErrorCode.TEAM_FORBIDDEN))
                .when(teamAccessGuard).requireLeader(post, SecurityTestFixtures.USER_A_ID);

        assertThatThrownBy(() ->
                applicationService.approveApplication(1L, SecurityTestFixtures.USER_A_ID, 9L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FORBIDDEN.getCode());
    }

    @Test
    void reject_success_notifiesApplicant() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        TeamMemberEntity member = pendingApply(SecurityTestFixtures.USER_A_ID);
        member.setId(9L);
        when(teamAccessGuard.requirePost(1L)).thenReturn(post);
        when(teamAccessGuard.requirePendingApplication(9L, 1L)).thenReturn(member);

        applicationService.rejectApplication(1L, SecurityTestFixtures.USER_B_ID, 9L);

        verify(teamMemberMapper).deleteById(9L);
        verify(teamNotificationService).notifyApplicationRejected(
                SecurityTestFixtures.USER_B_ID,
                SecurityTestFixtures.USER_A_ID,
                post);
    }

    @Test
    void approve_whenFull_throws() {
        TeamPostEntity post = recruitingPost(SecurityTestFixtures.USER_B_ID);
        when(teamAccessGuard.requireRecruitingPost(1L)).thenReturn(post);
        when(teamAccessGuard.requirePendingApplication(9L, 1L)).thenReturn(pendingApply(SecurityTestFixtures.USER_A_ID));
        when(lifecycleService.countAccepted(1L)).thenReturn(3);

        assertThatThrownBy(() ->
                applicationService.approveApplication(1L, SecurityTestFixtures.USER_B_ID, 9L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.TEAM_FULL.getCode());

        verify(teamMemberMapper, never()).acceptInvite(any(), any());
    }

    private static TeamPostEntity recruitingPost(Long leaderId) {
        TeamPostEntity post = new TeamPostEntity();
        post.setId(1L);
        post.setUserId(leaderId);
        post.setTitle("Test Team");
        post.setStatus(1);
        post.setMemberLimit(3);
        return post;
    }

    private static TeamMemberEntity pendingApply(Long userId) {
        TeamMemberEntity member = new TeamMemberEntity();
        member.setUserId(userId);
        member.setStatus(0);
        member.setPendingKind(PendingKind.APPLY);
        return member;
    }
}
