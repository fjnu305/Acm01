package org.fjnu305.acm01.module.team;

import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberLogMapper;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.fjnu305.acm01.module.team.service.TeamMemberLifecycleService;
import org.fjnu305.acm01.module.team.service.TeamNotificationService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamMemberLifecycleServiceTest {

    @Mock
    private TeamPostMapper teamPostMapper;
    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private TeamMemberLogMapper teamMemberLogMapper;
    @Mock
    private TeamNotificationService teamNotificationService;

    @InjectMocks
    private TeamMemberLifecycleService lifecycleService;

    @Test
    void syncAfterMembershipChange_whenFull_rejectsRemainingApplications() {
        TeamPostEntity post = new TeamPostEntity();
        post.setId(1L);
        post.setUserId(SecurityTestFixtures.USER_B_ID);
        post.setTitle("Team");
        post.setStatus(1);
        post.setMemberLimit(3);

        TeamMemberEntity pendingApply = new TeamMemberEntity();
        pendingApply.setId(10L);
        pendingApply.setUserId(SecurityTestFixtures.USER_A_ID);
        pendingApply.setPendingKind(PendingKind.APPLY);

        when(teamPostMapper.selectById(1L)).thenReturn(post);
        when(teamMemberMapper.countAcceptedByTeamPostId(1L)).thenReturn(3);
        when(teamMemberMapper.selectPendingByTeamPostId(1L)).thenReturn(List.of(pendingApply));

        lifecycleService.syncAfterMembershipChange(1L);

        verify(teamPostMapper).updateRecruitmentStatus(1L, 2);
        verify(teamMemberMapper).deleteById(10L);
        verify(teamNotificationService).notifyApplicationAutoRejectedFull(
                eq(SecurityTestFixtures.USER_B_ID),
                eq(SecurityTestFixtures.USER_A_ID),
                eq(post));
    }
}
