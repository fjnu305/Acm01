package org.fjnu305.acm01.module.social.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.social.dto.CreatePostRequest;
import org.fjnu305.acm01.module.social.entity.LikeEntity;
import org.fjnu305.acm01.module.social.entity.PostEntity;
import org.fjnu305.acm01.module.social.entity.TopicEntity;
import org.fjnu305.acm01.module.social.mapper.LikeMapper;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.fjnu305.acm01.module.social.mapper.TopicMapper;
import org.fjnu305.acm01.module.social.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final TopicMapper topicMapper;
    private final LikeMapper likeMapper;
    private final FeedService feedService;
    private final PostFollowNotifyService postFollowNotifyService;
    private final PostAccessGuard postAccessGuard;

    @Transactional
    public PostVO createPost(Long userId, CreatePostRequest request) {
        String content = request.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException(ErrorCode.POST_CONTENT_EMPTY);
        }

        if (request.getTopicId() != null) {
            TopicEntity topic = topicMapper.selectById(request.getTopicId());
            if (topic == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
        }

        PostEntity entity = new PostEntity();
        entity.setUserId(userId);
        entity.setContent(content);
        entity.setTopicId(request.getTopicId());
        postMapper.insert(entity);

        if (request.getTopicId() != null) {
            topicMapper.incrementPostCount(request.getTopicId());
        }

        feedService.evictHotFeed();
        postFollowNotifyService.notifyFollowersNewPost(userId, entity.getId(), content);
        return postMapper.selectVoById(entity.getId(), userId);
    }

    public PostVO getPost(Long postId, Long viewerId) {
        PostVO vo = postMapper.selectVoById(postId, viewerId);
        if (vo == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return vo;
    }

    public List<PostVO> getHotFeed(Long viewerId) {
        return feedService.getHotFeed(viewerId);
    }

    public PageResult<PostVO> listPosts(Long viewerId, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (safePageNum - 1) * safePageSize;

        List<PostVO> list = postMapper.selectPage(viewerId, offset, safePageSize);
        long total = postMapper.countActive();
        return PageResult.of(list, total, safePageNum, safePageSize);
    }

    @Transactional
    public void deleteOwnPost(Long userId, Long postId) {
        postAccessGuard.requireOwnedActivePost(userId, postId);
        postMapper.markDeleted(postId);
        feedService.evictHotFeed();
    }

    @Transactional
    public void adminDeletePost(Long postId) {
        postAccessGuard.requireActivePost(postId);
        postMapper.markDeleted(postId);
        feedService.evictHotFeed();
    }

    @Transactional
    public PostVO likePost(Long userId, Long postId) {
        postAccessGuard.requireActivePost(postId);
        if (!likeMapper.exists(postId, userId)) {
            LikeEntity like = new LikeEntity();
            like.setPostId(postId);
            like.setUserId(userId);
            likeMapper.insert(like);
            postMapper.incrementLikeCount(postId);
            feedService.evictHotFeed();
        }
        return postMapper.selectVoById(postId, userId);
    }

    @Transactional
    public PostVO unlikePost(Long userId, Long postId) {
        postAccessGuard.requireActivePost(postId);
        if (likeMapper.exists(postId, userId)) {
            likeMapper.delete(postId, userId);
            postMapper.decrementLikeCount(postId);
            feedService.evictHotFeed();
        }
        return postMapper.selectVoById(postId, userId);
    }
}
