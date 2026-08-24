package org.fjnu305.acm01.module.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.social.entity.LikeEntity;

import java.util.Collection;
import java.util.List;

@Mapper
public interface LikeMapper {

    int insert(LikeEntity entity);

    int delete(@Param("postId") Long postId, @Param("userId") Long userId);

    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);

    List<Long> selectLikedPostIds(@Param("userId") Long userId,
                                  @Param("postIds") Collection<Long> postIds);
}
