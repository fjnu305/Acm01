package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.team.domain.MemberStatus;
import org.fjnu305.acm01.module.team.domain.PendingKind;
import org.fjnu305.acm01.module.team.domain.TeamPostPolicy;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamAccessGuard {

    private final TeamPostMapper teamPostMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMemberLifecycleService lifecycleService;

    public TeamPostEntity requirePost(Long id) {
        TeamPostEntity post = teamPostMapper.selectById(id);
        if (post == null || post.getDeleted() != null && post.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        return post;
    }

    public TeamPostEntity requireRecruitingPost(Long id) {
        TeamPostEntity post = requirePost(id);
        int accepted = lifecycleService.countAccepted(id);
        if (!TeamPostPolicy.canRecruit(post, accepted)) {
            if (TeamPostPolicy.isManuallyClosed(post)) {
                throw new BusinessException(ErrorCode.TEAM_NOT_RECRUITING);
            }
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }
        return post;
    }

    public void requireLeader(TeamPostEntity post, Long userId) {
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.TEAM_FORBIDDEN);
        }
    }

    public void requireLeaderOrMember(TeamPostEntity post, Long userId) {
        if (post.getUserId().equals(userId)) {
            return;
        }
        TeamMemberEntity member = teamMemberMapper.selectByTeamAndUser(post.getId(), userId);
        if (member == null || member.getStatus() != MemberStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.TEAM_FORBIDDEN);
        }
    }

    public TeamMemberEntity requirePendingApplication(Long memberId, Long teamPostId) {
        TeamMemberEntity member = teamMemberMapper.selectById(memberId);
        if (member == null
                || !member.getTeamPostId().equals(teamPostId)
                || member.getStatus() == null
                || member.getStatus() != MemberStatus.PENDING
                || !PendingKind.isApply(member.getPendingKind())) {
            throw new BusinessException(ErrorCode.TEAM_APPLICATION_NOT_FOUND);
        }
        return member;
    }

    public TeamMemberEntity requirePendingInvite(Long memberId, Long userId) {
        TeamMemberEntity member = teamMemberMapper.selectById(memberId);
        if (member == null || !member.getUserId().equals(userId) || member.getStatus() != MemberStatus.PENDING) {
            throw new BusinessException(ErrorCode.TEAM_INVITE_NOT_FOUND);
        }
        if (PendingKind.isApply(member.getPendingKind())) {
            throw new BusinessException(ErrorCode.TEAM_FORBIDDEN, "Wait for leader approval");
        }
        return member;
    }
}
