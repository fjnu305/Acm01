package org.fjnu305.acm01.module.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.social.entity.PostEntity;
import org.fjnu305.acm01.module.social.vo.PostVO;

import java.util.List;

@Mapper
public interface PostMapper {

    int insert(PostEntity entity);

    PostEntity selectById(@Param("id") Long id);

    PostVO selectVoById(@Param("id") Long id, @Param("viewerId") Long viewerId);

    List<PostVO> selectHotFeed(@Param("viewerId") Long viewerId, @Param("limit") int limit);

    List<PostVO> selectPage(@Param("viewerId") Long viewerId,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    long countActive();

    int markDeleted(@Param("id") Long id);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int incrementCommentCount(@Param("id") Long id);
}
