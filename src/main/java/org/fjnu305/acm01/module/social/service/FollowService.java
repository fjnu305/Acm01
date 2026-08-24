package org.fjnu305.acm01.module.social.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.social.entity.FollowEntity;
import org.fjnu305.acm01.module.social.mapper.FollowMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF);
        }
        if (userMapper.selectById(followeeId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!followMapper.exists(followerId, followeeId)) {
            FollowEntity entity = new FollowEntity();
            entity.setFollowerId(followerId);
            entity.setFolloweeId(followeeId);
            followMapper.insert(entity);
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followMapper.delete(followerId, followeeId);
    }

    public List<Long> listFollowing(Long followerId) {
        return followMapper.selectFolloweeIds(followerId);
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return false;
        }
        return followMapper.exists(followerId, followeeId);
    }

    public List<Long> listFollowerIds(Long followeeId) {
        return followMapper.selectFollowerIds(followeeId);
    }
}
