package org.fjnu305.acm01.module.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.user.entity.UserEntity;

import java.util.List;

@Mapper
public interface TeamCandidateMapper {

    List<UserEntity> selectCandidates(@Param("excludeUserIds") List<Long> excludeUserIds,
                                      @Param("ratingMin") int ratingMin,
                                      @Param("ratingMax") int ratingMax,
                                      @Param("limit") int limit);
}
