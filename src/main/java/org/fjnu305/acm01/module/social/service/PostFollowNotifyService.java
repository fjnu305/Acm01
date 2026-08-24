package org.fjnu305.acm01.module.social.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.util.TextUtils;
import org.fjnu305.acm01.module.inbox.constant.InboxRefType;
import org.fjnu305.acm01.module.inbox.service.InboxDeliveryService;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.module.user.support.UserDisplayNameResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostFollowNotifyService {

    private final FollowService followService;
    private final InboxDeliveryService inboxDeliveryService;
    private final UserMapper userMapper;
    private final UserDisplayNameResolver displayNames;

    public void notifyFollowersNewPost(Long authorId, Long postId, String content) {
        List<Long> followerIds = followService.listFollowerIds(authorId);
        if (followerIds.isEmpty()) {
            return;
        }
        UserEntity author = userMapper.selectById(authorId);
        if (author == null) {
            return;
        }
        String title = displayNames.resolve(author) + " 发布了新动态";
        String body = TextUtils.truncate(content, 200);
        for (Long followerId : followerIds) {
            if (followerId == null || followerId.equals(authorId)) {
                continue;
            }
            inboxDeliveryService.deliverWithRef(
                    authorId,
                    followerId,
                    title,
                    body,
                    InboxRefType.SOCIAL_POST,
                    postId);
        }
    }
}
