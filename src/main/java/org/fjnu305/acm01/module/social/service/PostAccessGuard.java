package org.fjnu305.acm01.module.social.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.social.entity.PostEntity;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostAccessGuard {

    private final PostMapper postMapper;

    public PostEntity requireActivePost(Long postId) {
        PostEntity post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    public PostEntity requireOwnedActivePost(Long userId, Long postId) {
        PostEntity post = requireActivePost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return post;
    }
}
