package org.fjnu305.acm01.module.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.team.entity.TeamMemberLogEntity;

@Mapper
public interface TeamMemberLogMapper {

    int insert(TeamMemberLogEntity entity);

    void log(@Param("teamPostId") Long teamPostId,
             @Param("userId") Long userId,
             @Param("action") String action,
             @Param("actorId") Long actorId);
}
