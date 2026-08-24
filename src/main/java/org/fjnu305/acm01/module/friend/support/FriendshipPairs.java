package org.fjnu305.acm01.module.friend.support;

import org.fjnu305.acm01.module.friend.entity.FriendshipEntity;

public final class FriendshipPairs {

    private FriendshipPairs() {
    }

    public static FriendshipEntity of(Long userA, Long userB) {
        FriendshipEntity entity = new FriendshipEntity();
        entity.setUserLowId(Math.min(userA, userB));
        entity.setUserHighId(Math.max(userA, userB));
        return entity;
    }
}
