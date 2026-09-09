package org.fjnu305.acm01.module.contest.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;

import java.util.List;

@Mapper
public interface ContestQueryMapper {

    List<ContestEntity> selectPage(@Param("source") String source,
                                   @Param("status") Integer status,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    long countPage(@Param("source") String source,
                   @Param("status") Integer status);

    ContestEntity selectById(@Param("id") Long id);

    List<ContestEntity> selectByIds(@Param("ids") List<Long> ids);
}
