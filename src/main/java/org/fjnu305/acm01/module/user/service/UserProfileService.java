package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.service.OfficialUserService;
import org.fjnu305.acm01.module.social.service.FollowService;
import org.fjnu305.acm01.module.sync.service.OjAccountService;
import org.fjnu305.acm01.module.user.dto.PublicProfileVO;
import org.fjnu305.acm01.module.user.dto.UserSearchVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserService userService;
    private final FriendService friendService;
    private final OfficialUserService officialUserService;
    private final FollowService followService;
    private final UserMapper userMapper;
    private final OjAccountService ojAccountService;

    public PublicProfileVO getPublicProfile(Long targetUserId, Long viewerId) {
        UserEntity user = userService.requirePublicUser(targetUserId);
        boolean isSelf = viewerId != null && viewerId.equals(targetUserId);
        return PublicProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .school(user.getSchool())
                .bio(user.getBio())
                .cfHandle(user.getCfHandle())
                .cfRating(user.getCfRating())
                .solvedCount(user.getSolvedCount())
                .isSelf(isSelf)
                .friendStatus(isSelf ? null : friendService.resolveFriendStatus(viewerId, targetUserId))
                .official(officialUserService.isOfficialUser(targetUserId))
                .following(viewerId != null && !isSelf && followService.isFollowing(viewerId, targetUserId))
                .cfRatingHistory(ojAccountService.listCfRatingHistory(targetUserId))
                .build();
    }

    public List<UserSearchVO> searchUsers(Long viewerId, String keyword, int limit) {
        if (viewerId == null) {
            return List.of();
        }
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String trimmed = keyword.trim();
        if (trimmed.length() < 1) {
            return List.of();
        }
        int size = Math.min(Math.max(limit, 1), 30);
        List<UserEntity> users = userMapper.selectSearchCandidates(trimmed, viewerId, size);
        List<UserSearchVO> result = new ArrayList<>(users.size());
        for (UserEntity user : users) {
            result.add(UserSearchVO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .school(user.getSchool())
                    .friendStatus(friendService.resolveFriendStatus(viewerId, user.getId()))
                    .official(officialUserService.isOfficialUser(user.getId()))
                    .build());
        }
        return result;
    }
}
