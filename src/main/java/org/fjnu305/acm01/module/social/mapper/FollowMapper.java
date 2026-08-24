package org.fjnu305.acm01.module.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.social.entity.FollowEntity;

import java.util.List;

@Mapper
public interface FollowMapper {

    int insert(FollowEntity entity);

    int delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    boolean exists(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    List<Long> selectFolloweeIds(@Param("followerId") Long followerId);

    List<Long> selectFollowerIds(@Param("followeeId") Long followeeId);
}
