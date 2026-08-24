package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.domain.TeamPostPolicy;
import org.fjnu305.acm01.module.team.domain.TeamPostStatus;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberLogMapper;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberLifecycleService {

    public static final String ACTION_AUTO_REJECT_FULL = "AUTO_REJECT_FULL";

    private final TeamPostMapper teamPostMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMemberLogMapper teamMemberLogMapper;
    private final TeamNotificationService teamNotificationService;

    public int countAccepted(Long teamPostId) {
        return teamMemberMapper.countAcceptedByTeamPostId(teamPostId);
    }

    public TeamPostStatus effectiveStatus(TeamPostEntity post) {
        return TeamPostPolicy.effectiveStatus(post, countAccepted(post.getId()));
    }

    public void syncAfterMembershipChange(Long teamPostId) {
        TeamPostEntity post = teamPostMapper.selectById(teamPostId);
        if (post == null) {
            return;
        }
        int accepted = countAccepted(teamPostId);
        TeamPostStatus effective = TeamPostPolicy.effectiveStatus(post, accepted);
        int stored = TeamPostStatus.toStored(effective, post.getStatus() == null ? 1 : post.getStatus());
        teamPostMapper.updateRecruitmentStatus(teamPostId, stored);
        if (effective == TeamPostStatus.FULL) {
            rejectRemainingPendingWhenFull(post);
        }
    }

    public void logAction(Long teamPostId, Long userId, String action, Long actorId) {
        teamMemberLogMapper.log(teamPostId, userId, action, actorId);
    }

    private void rejectRemainingPendingWhenFull(TeamPostEntity post) {
        if (!TeamPostPolicy.effectiveStatus(post, countAccepted(post.getId())).equals(TeamPostStatus.FULL)) {
            return;
        }
        List<TeamMemberEntity> pending = teamMemberMapper.selectPendingByTeamPostId(post.getId());
        for (TeamMemberEntity member : pending) {
            teamMemberMapper.deleteById(member.getId());
            teamMemberLogMapper.log(post.getId(), member.getUserId(), ACTION_AUTO_REJECT_FULL, post.getUserId());
            if (PendingKind.isApply(member.getPendingKind())) {
                teamNotificationService.notifyApplicationAutoRejectedFull(
                        post.getUserId(), member.getUserId(), post);
            }
        }
    }
}
