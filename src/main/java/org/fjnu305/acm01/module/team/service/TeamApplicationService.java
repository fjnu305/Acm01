package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
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
public class TeamApplicationService {

    private final TeamMemberMapper teamMemberMapper;
    private final TeamAccessGuard teamAccessGuard;
    private final TeamMemberLifecycleService lifecycleService;
    private final TeamNotificationService teamNotificationService;

    @Transactional
    public void apply(Long teamPostId, Long userId) {
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(teamPostId);
        if (post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot apply to your own team");
        }
        TeamMemberEntity existing = teamMemberMapper.selectByTeamAndUser(teamPostId, userId);
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == MemberStatus.ACCEPTED) {
                throw new BusinessException(ErrorCode.TEAM_ALREADY_MEMBER);
            }
            if (PendingKind.isApply(existing.getPendingKind())) {
                throw new BusinessException(ErrorCode.TEAM_ALREADY_APPLIED);
            }
            throw new BusinessException(ErrorCode.TEAM_PENDING_INVITE_EXISTS);
        }
        assertHasSlot(teamPostId, post);

        TeamMemberEntity application = new TeamMemberEntity();
        application.setTeamPostId(teamPostId);
        application.setUserId(userId);
        application.setRole(MemberRole.MEMBER);
        application.setStatus(MemberStatus.PENDING);
        application.setPendingKind(PendingKind.APPLY);
        teamMemberMapper.insert(application);
        lifecycleService.logAction(teamPostId, userId, "APPLY", userId);
        teamNotificationService.notifyApplicationSubmitted(userId, post.getUserId(), post, application.getId());
    }

    @Transactional
    public void approveApplication(Long teamPostId, Long leaderId, Long memberId) {
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(teamPostId);
        teamAccessGuard.requireLeader(post, leaderId);
        TeamMemberEntity member = teamAccessGuard.requirePendingApplication(memberId, teamPostId);
        assertHasSlot(teamPostId, post);

        int accepted = teamMemberMapper.acceptInvite(memberId, member.getUserId());
        if (accepted == 0) {
            throw new BusinessException(ErrorCode.TEAM_APPLICATION_NOT_FOUND);
        }
        lifecycleService.logAction(teamPostId, member.getUserId(), "APPROVE", leaderId);
        teamNotificationService.notifyApplicationApproved(leaderId, member.getUserId(), post);
        lifecycleService.syncAfterMembershipChange(post.getId());
    }

    /** Inbox 等场景仅持有 memberId 时使用。 */
    @Transactional
    public void approveApplicationByMemberId(Long leaderId, Long memberId) {
        TeamMemberEntity member = requireMemberRow(memberId);
        approveApplication(member.getTeamPostId(), leaderId, memberId);
    }

    @Transactional
    public void rejectApplication(Long teamPostId, Long leaderId, Long memberId) {
        TeamPostEntity post = teamAccessGuard.requirePost(teamPostId);
        teamAccessGuard.requireLeader(post, leaderId);
        TeamMemberEntity member = teamAccessGuard.requirePendingApplication(memberId, teamPostId);
        teamMemberMapper.deleteById(memberId);
        lifecycleService.logAction(teamPostId, member.getUserId(), "REJECT", leaderId);
        teamNotificationService.notifyApplicationRejected(leaderId, member.getUserId(), post);
    }

    @Transactional
    public void rejectApplicationByMemberId(Long leaderId, Long memberId) {
        TeamMemberEntity member = requireMemberRow(memberId);
        rejectApplication(member.getTeamPostId(), leaderId, memberId);
    }

    private TeamMemberEntity requireMemberRow(Long memberId) {
        TeamMemberEntity member = teamMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.TEAM_APPLICATION_NOT_FOUND);
        }
        return member;
    }

    private void assertHasSlot(Long teamPostId, TeamPostEntity post) {
        int accepted = lifecycleService.countAccepted(teamPostId);
        if (!TeamCapacity.hasSlot(accepted, post.getMemberLimit())) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }
    }
}
