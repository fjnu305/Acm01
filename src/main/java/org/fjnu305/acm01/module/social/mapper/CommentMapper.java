package org.fjnu305.acm01.module.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.social.entity.CommentEntity;
import org.fjnu305.acm01.module.social.vo.CommentVO;

import java.util.List;

@Mapper
public interface CommentMapper {

    int insert(CommentEntity entity);

    CommentEntity selectById(@Param("id") Long id);

    List<CommentVO> selectByPostId(@Param("postId") Long postId);

    CommentVO selectVoById(@Param("id") Long id);

    int markDeleted(@Param("id") Long id);
}
