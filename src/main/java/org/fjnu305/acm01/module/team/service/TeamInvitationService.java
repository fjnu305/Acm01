package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.team.domain.MemberRole;
import org.fjnu305.acm01.module.team.domain.MemberStatus;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.domain.TeamCapacity;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamInvitationService {

    private final TeamMemberMapper teamMemberMapper;
    private final TeamAccessGuard teamAccessGuard;
    private final TeamMemberLifecycleService lifecycleService;
    private final TeamNotificationService teamNotificationService;
    private final FriendService friendService;

    @Transactional
    public void invite(Long teamPostId, Long leaderId, Long targetUserId) {
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(teamPostId);
        teamAccessGuard.requireLeader(post, leaderId);
        if (targetUserId.equals(leaderId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot invite yourself");
        }
        friendService.requireFriendship(leaderId, targetUserId);
        if (teamMemberMapper.selectByTeamAndUser(teamPostId, targetUserId) != null) {
            throw new BusinessException(ErrorCode.TEAM_ALREADY_MEMBER);
        }
        assertHasSlot(teamPostId, post);

        TeamMemberEntity invite = new TeamMemberEntity();
        invite.setTeamPostId(teamPostId);
        invite.setUserId(targetUserId);
        invite.setRole(MemberRole.MEMBER);
        invite.setStatus(MemberStatus.PENDING);
        invite.setPendingKind(PendingKind.INVITE);
        teamMemberMapper.insert(invite);
        lifecycleService.logAction(teamPostId, targetUserId, "INVITE", leaderId);

        String body = post.getDescription() != null && !post.getDescription().isBlank()
                ? post.getDescription().trim()
                : "一起来组队吧！";
        teamNotificationService.notifyInvite(leaderId, targetUserId, post, invite.getId(), body);
    }

    @Transactional
    public void acceptInvite(Long memberId, Long userId) {
        TeamMemberEntity member = teamAccessGuard.requirePendingInvite(memberId, userId);
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(member.getTeamPostId());
        assertHasSlot(post.getId(), post);

        int accepted = teamMemberMapper.acceptInvite(memberId, userId);
        if (accepted == 0) {
            throw new BusinessException(ErrorCode.TEAM_INVITE_NOT_FOUND);
        }
        lifecycleService.logAction(post.getId(), userId, "ACCEPT", userId);
        lifecycleService.syncAfterMembershipChange(post.getId());
    }

    @Transactional
    public void declineInvite(Long memberId, Long userId) {
        TeamMemberEntity member = teamAccessGuard.requirePendingInvite(memberId, userId);
        teamMemberMapper.deleteById(memberId);
        lifecycleService.logAction(member.getTeamPostId(), userId, "DECLINE", userId);
    }

    private void assertHasSlot(Long teamPostId, TeamPostEntity post) {
        int accepted = lifecycleService.countAccepted(teamPostId);
        if (!TeamCapacity.hasSlot(accepted, post.getMemberLimit())) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }
    }
}
