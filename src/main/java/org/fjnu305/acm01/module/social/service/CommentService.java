package org.fjnu305.acm01.module.social.service;



import lombok.RequiredArgsConstructor;

import org.fjnu305.acm01.Common.exception.BusinessException;

import org.fjnu305.acm01.Common.exception.ErrorCode;

import org.fjnu305.acm01.module.social.dto.CreateCommentRequest;

import org.fjnu305.acm01.module.social.entity.CommentEntity;

import org.fjnu305.acm01.module.social.mapper.CommentMapper;

import org.fjnu305.acm01.module.social.mapper.PostMapper;

import org.fjnu305.acm01.module.social.vo.CommentVO;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.List;



@Service

@RequiredArgsConstructor

public class CommentService {



    private final CommentMapper commentMapper;

    private final PostMapper postMapper;

    private final FeedService feedService;

    private final PostAccessGuard postAccessGuard;



    @Transactional

    public CommentVO createComment(Long userId, Long postId, CreateCommentRequest request) {

        postAccessGuard.requireActivePost(postId);



        String content = request.getContent().trim();

        if (content.isEmpty()) {

            throw new BusinessException(ErrorCode.BAD_REQUEST);

        }



        if (request.getParentId() != null) {

            CommentEntity parent = commentMapper.selectById(request.getParentId());

            if (parent == null || parent.getStatus() != 1 || !parent.getPostId().equals(postId)) {

                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);

            }

        }



        CommentEntity entity = new CommentEntity();

        entity.setPostId(postId);

        entity.setUserId(userId);

        entity.setContent(content);

        entity.setParentId(request.getParentId());

        commentMapper.insert(entity);

        postMapper.incrementCommentCount(postId);

        feedService.evictHotFeed();



        CommentVO created = commentMapper.selectVoById(entity.getId());

        if (created == null) {

            throw new BusinessException(ErrorCode.INTERNAL_ERROR);

        }

        return created;

    }



    public List<CommentVO> listComments(Long postId) {

        postAccessGuard.requireActivePost(postId);

        return commentMapper.selectByPostId(postId);

    }

}

