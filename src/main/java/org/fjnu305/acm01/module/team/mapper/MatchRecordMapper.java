package org.fjnu305.acm01.module.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.team.entity.MatchRecordEntity;

import java.util.List;

@Mapper
public interface MatchRecordMapper {

    int insert(MatchRecordEntity entity);

    int deleteByTeamPostId(@Param("teamPostId") Long teamPostId);

    List<MatchRecordEntity> selectByTeamPostId(@Param("teamPostId") Long teamPostId);
}
