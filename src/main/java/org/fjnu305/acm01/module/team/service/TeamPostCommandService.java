package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.team.domain.MemberRole;
import org.fjnu305.acm01.module.team.domain.MemberStatus;
import org.fjnu305.acm01.module.team.domain.TeamPostPolicy;
import org.fjnu305.acm01.module.team.dto.TeamPublishRequest;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.fjnu305.acm01.module.team.vo.TeamPostDetailVO;
import org.fjnu305.acm01.module.search.support.SearchDocumentFactory;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamPostCommandService {

    private final TeamPostMapper teamPostMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMemberLifecycleService lifecycleService;
    private final TeamQueryService teamQueryService;
    private final UserMapper userMapper;
    private final SearchDocumentFactory searchDocumentFactory;

    @Transactional
    public TeamPostDetailVO publish(Long userId, TeamPublishRequest request) {
        if (request.getRatingMin() > request.getRatingMax()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ratingMin cannot exceed ratingMax");
        }

        TeamPostEntity entity = new TeamPostEntity();
        entity.setUserId(userId);
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(request.getDescription());
        entity.setRatingMin(request.getRatingMin());
        entity.setRatingMax(request.getRatingMax());
        entity.setRegion(request.getRegion());
        entity.setTags(normalizeTags(request.getTags()));
        entity.setMemberLimit(request.getMemberLimit());
        entity.setStatus(1);
        teamPostMapper.insert(entity);

        TeamMemberEntity leader = new TeamMemberEntity();
        leader.setTeamPostId(entity.getId());
        leader.setUserId(userId);
        leader.setRole(MemberRole.LEADER);
        leader.setStatus(MemberStatus.ACCEPTED);
        teamMemberMapper.insert(leader);
        lifecycleService.logAction(entity.getId(), userId, "PUBLISH", userId);

        syncSearch(entity);
        return teamQueryService.getDetail(entity.getId());
    }

    @Transactional
    public void closeRecruitment(Long teamPostId, Long leaderId) {
        TeamPostEntity post = requireOwnedPost(teamPostId, leaderId);
        if (TeamPostPolicy.isManuallyClosed(post)) {
            throw new BusinessException(ErrorCode.TEAM_NOT_RECRUITING);
        }
        teamPostMapper.updateRecruitmentStatus(teamPostId, 0);
        lifecycleService.logAction(teamPostId, leaderId, "CLOSE", leaderId);
        searchDocumentFactory.deleteTeamPost(teamPostId);
    }

    @Transactional
    public void dissolveTeam(Long teamPostId, Long leaderId) {
        requireOwnedPost(teamPostId, leaderId);
        teamPostMapper.softDelete(teamPostId);
        lifecycleService.logAction(teamPostId, leaderId, "DISSOLVE", leaderId);
        searchDocumentFactory.deleteTeamPost(teamPostId);
    }

    private TeamPostEntity requireOwnedPost(Long teamPostId, Long leaderId) {
        TeamPostEntity post = teamPostMapper.selectById(teamPostId);
        if (post == null || post.getDeleted() != null && post.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (!post.getUserId().equals(leaderId)) {
            throw new BusinessException(ErrorCode.TEAM_FORBIDDEN);
        }
        return post;
    }

    private void syncSearch(TeamPostEntity entity) {
        UserEntity author = userMapper.selectById(entity.getUserId());
        String authorName = author == null ? "" : (author.getNickname() != null ? author.getNickname() : author.getUsername());
        searchDocumentFactory.indexTeamPost(entity, authorName);
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return tags.replace("，", ",").replace(" ", "");
    }
}
