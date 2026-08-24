package org.fjnu305.acm01.module.friend.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.friend.entity.FriendshipEntity;
import org.fjnu305.acm01.module.friend.mapper.FriendshipMapper;
import org.fjnu305.acm01.module.friend.support.FriendshipPairs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipBootstrapService {

    private final FriendshipMapper friendshipMapper;
    private final OfficialUserService officialUserService;

    @Transactional
    public void ensureOfficialFriend(Long userId) {
        if (userId == null || officialUserService.isOfficialUser(userId)) {
            return;
        }
        Long officialId = officialUserService.requireOfficialUserId();
        if (!friendshipMapper.exists(userId, officialId)) {
            FriendshipEntity pair = FriendshipPairs.of(userId, officialId);
            friendshipMapper.insert(pair);
        }
    }
}
