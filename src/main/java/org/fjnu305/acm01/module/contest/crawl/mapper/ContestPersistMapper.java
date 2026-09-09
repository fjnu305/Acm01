package org.fjnu305.acm01.module.contest.crawl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;

import java.util.List;

@Mapper
public interface ContestPersistMapper {

    ContestEntity selectBySourceAndExternalId(@Param("source") String source,
                                              @Param("externalId") String externalId);

    int insert(ContestEntity entity);

    int updateBySourceAndExternalId(ContestEntity entity);

    List<ContestEntity> selectRawHashByKeys(@Param("list") List<ContestEntity> list);

    List<ContestEntity> selectSnapshotByKeys(@Param("list") List<ContestEntity> list);

    int batchInsert(@Param("list") List<ContestEntity> list);

    int batchUpdateBySourceAndExternalId(@Param("list") List<ContestEntity> list);
}
