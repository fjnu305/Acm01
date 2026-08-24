package org.fjnu305.acm01.module.contest.log.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.contest.log.entity.ContestCrawlLogEntity;

import java.util.List;

@Mapper
public interface ContestCrawlLogMapper {

    int insert(ContestCrawlLogEntity log);

    List<ContestCrawlLogEntity> selectRecentBySource(@Param("source") String source,
                                                     @Param("limit") int limit);

    long countPage(@Param("source") String source, @Param("status") String status);

    List<ContestCrawlLogEntity> selectPage(@Param("source") String source,
                                           @Param("status") String status,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);
}
