package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.friend.vo.FriendVO;
import org.fjnu305.acm01.module.team.dto.TeamPublishRequest;
import org.fjnu305.acm01.module.team.vo.TeamPostDetailVO;
import org.fjnu305.acm01.module.team.vo.TeamPostVO;
import org.fjnu305.acm01.module.team.vo.TeamRecommendVO;
import org.fjnu305.acm01.Common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组队模块门面：委托给各子服务，Controller 仅依赖此类。
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamPostCommandService commandService;
    private final TeamQueryService queryService;
    private final TeamInvitationService invitationService;
    private final TeamApplicationService applicationService;

    public TeamPostDetailVO publish(Long userId, TeamPublishRequest request) {
        return commandService.publish(userId, request);
    }

    public PageResult<TeamPostVO> list(Integer status, String region, int pageNum, int pageSize) {
        return queryService.list(status, region, pageNum, pageSize);
    }

    public PageResult<TeamPostVO> listMine(Long userId, int pageNum, int pageSize) {
        return queryService.listMine(userId, pageNum, pageSize);
    }

    public TeamPostDetailVO getDetail(Long id) {
        return queryService.getDetail(id);
    }

    public List<TeamRecommendVO> recommend(Long teamPostId, Long requesterId) {
        return queryService.recommend(teamPostId, requesterId);
    }

    public List<FriendVO> listInvitableFriends(Long teamPostId, Long leaderId) {
        return queryService.listInvitableFriends(teamPostId, leaderId);
    }

    public void invite(Long teamPostId, Long leaderId, Long targetUserId) {
        invitationService.invite(teamPostId, leaderId, targetUserId);
    }

    public void apply(Long teamPostId, Long userId) {
        applicationService.apply(teamPostId, userId);
    }

    public void approveApplication(Long teamPostId, Long leaderId, Long memberId) {
        applicationService.approveApplication(teamPostId, leaderId, memberId);
    }

    public void approveApplicationByMemberId(Long leaderId, Long memberId) {
        applicationService.approveApplicationByMemberId(leaderId, memberId);
    }

    public void rejectApplication(Long teamPostId, Long leaderId, Long memberId) {
        applicationService.rejectApplication(teamPostId, leaderId, memberId);
    }

    public void rejectApplicationByMemberId(Long leaderId, Long memberId) {
        applicationService.rejectApplicationByMemberId(leaderId, memberId);
    }

    public void acceptInvite(Long memberId, Long userId) {
        invitationService.acceptInvite(memberId, userId);
    }

    public void declineInvite(Long memberId, Long userId) {
        invitationService.declineInvite(memberId, userId);
    }

    public void closeRecruitment(Long teamPostId, Long leaderId) {
        commandService.closeRecruitment(teamPostId, leaderId);
    }

    public void dissolveTeam(Long teamPostId, Long leaderId) {
        commandService.dissolveTeam(teamPostId, leaderId);
    }
}
