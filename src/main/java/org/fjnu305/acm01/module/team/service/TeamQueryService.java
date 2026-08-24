package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.util.PageParams;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.fjnu305.acm01.module.team.vo.TeamPostDetailVO;
import org.fjnu305.acm01.module.team.vo.TeamPostVO;
import org.fjnu305.acm01.module.team.vo.TeamRecommendVO;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.vo.FriendVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamQueryService {

    private final TeamPostMapper teamPostMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMatchService teamMatchService;
    private final FriendService friendService;
    private final TeamAccessGuard teamAccessGuard;

    public PageResult<TeamPostVO> list(Integer status, String region, int pageNum, int pageSize) {
        PageParams page = PageParams.of(pageNum, pageSize);
        Integer statusFilter = status == null ? 1 : status;
        List<TeamPostVO> list = teamPostMapper.selectPage(statusFilter, region, page.offset(), page.size());
        attachMemberPreviews(list);
        long total = teamPostMapper.countPage(statusFilter, region);
        return PageResult.of(list, total, page.page(), page.size());
    }

    public PageResult<TeamPostVO> listMine(Long userId, int pageNum, int pageSize) {
        PageParams page = PageParams.of(pageNum, pageSize);
        List<TeamPostVO> list = teamPostMapper.selectMinePage(userId, page.offset(), page.size());
        attachMemberPreviews(list);
        long total = teamPostMapper.countMinePage(userId);
        return PageResult.of(list, total, page.page(), page.size());
    }

    public TeamPostDetailVO getDetail(Long id) {
        TeamPostDetailVO detail = teamPostMapper.selectDetail(id);
        if (detail == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        detail.setMembers(teamMemberMapper.selectByTeamPostId(id));
        return detail;
    }

    public List<TeamRecommendVO> recommend(Long teamPostId, Long requesterId) {
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(teamPostId);
        teamAccessGuard.requireLeaderOrMember(post, requesterId);
        return teamMatchService.recommend(post);
    }

    public List<FriendVO> listInvitableFriends(Long teamPostId, Long leaderId) {
        TeamPostEntity post = teamAccessGuard.requireRecruitingPost(teamPostId);
        teamAccessGuard.requireLeader(post, leaderId);
        Set<Long> memberIds = Set.copyOf(teamMemberMapper.selectMemberUserIds(teamPostId));
        return friendService.listFriends(leaderId).stream()
                .filter(friend -> !Boolean.TRUE.equals(friend.getOfficial()))
                .filter(friend -> !memberIds.contains(friend.getUserId()))
                .collect(Collectors.toList());
    }

    private void attachMemberPreviews(List<TeamPostVO> list) {
        for (TeamPostVO item : list) {
            item.setMemberPreview(teamMemberMapper.selectAcceptedPreviewByTeamPostId(item.getId()));
        }
    }
}
