package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.team.config.TeamMatchProperties;
import org.fjnu305.acm01.module.team.entity.MatchRecordEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.MatchRecordMapper;
import org.fjnu305.acm01.module.team.mapper.TeamCandidateMapper;
import org.fjnu305.acm01.module.team.mapper.TeamMemberMapper;
import org.fjnu305.acm01.module.team.vo.TeamRecommendVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMatchService {

    private final TeamMatchProperties teamMatchProperties;
    private final TeamCandidateMapper teamCandidateMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final SolutionMapper solutionMapper;

    public List<TeamRecommendVO> recommend(TeamPostEntity post) {
        List<Long> excludeIds = teamMemberMapper.selectMemberUserIds(post.getId());
        int ratingMin = post.getRatingMin() == null ? 0 : post.getRatingMin();
        int ratingMax = post.getRatingMax() == null ? 4000 : post.getRatingMax();
        int limit = Math.max(teamMatchProperties.getRecommendLimit(), 50);

        List<UserEntity> candidates = teamCandidateMapper.selectCandidates(excludeIds, ratingMin, ratingMax, limit);
        Set<String> teamTags = parseTags(post.getTags());
        int targetRating = (ratingMin + ratingMax) / 2;

        List<TeamRecommendVO> scored = new ArrayList<>();
        for (UserEntity user : candidates) {
            double ratingScore = ratingScore(user.getCfRating() == null ? 0 : user.getCfRating(), targetRating, ratingMax - ratingMin);
            double regionScore = regionScore(post.getRegion(), user.getSchool());
            double tagScore = tagScore(teamTags, parseTags(solutionMapper.selectAggregatedTagsByUserId(user.getId())));
            double total = teamMatchProperties.getRatingWeight() * ratingScore
                    + teamMatchProperties.getRegionWeight() * regionScore
                    + teamMatchProperties.getTagWeight() * tagScore;

            TeamRecommendVO vo = new TeamRecommendVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setCfRating(user.getCfRating());
            vo.setSchool(user.getSchool());
            vo.setMatchScore(BigDecimal.valueOf(total).setScale(4, RoundingMode.HALF_UP));
            scored.add(vo);
        }

        scored.sort(Comparator.comparing(TeamRecommendVO::getMatchScore).reversed());
        List<TeamRecommendVO> top = scored.stream()
                .limit(teamMatchProperties.getRecommendLimit())
                .collect(Collectors.toList());

        persistMatchRecords(post.getId(), top);
        return top;
    }

    private void persistMatchRecords(Long teamPostId, List<TeamRecommendVO> recommendations) {
        matchRecordMapper.deleteByTeamPostId(teamPostId);
        for (TeamRecommendVO item : recommendations) {
            MatchRecordEntity record = new MatchRecordEntity();
            record.setTeamPostId(teamPostId);
            record.setUserId(item.getUserId());
            record.setMatchScore(item.getMatchScore());
            matchRecordMapper.insert(record);
        }
    }

    private double ratingScore(int userRating, int targetRating, int range) {
        int span = Math.max(range, 200);
        double diff = Math.abs(userRating - targetRating);
        return Math.max(0, 1.0 - diff / span);
    }

    private double regionScore(String expectedRegion, String userSchool) {
        if (!StringUtils.hasText(expectedRegion) || !StringUtils.hasText(userSchool)) {
            return 0.5;
        }
        return expectedRegion.trim().equalsIgnoreCase(userSchool.trim()) ? 1.0 : 0.0;
    }

    private double tagScore(Set<String> required, Set<String> userTags) {
        if (required.isEmpty()) {
            return 0.5;
        }
        if (userTags.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(required);
        intersection.retainAll(userTags);
        Set<String> union = new HashSet<>(required);
        union.addAll(userTags);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private Set<String> parseTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
