package org.fjnu305.acm01.module.sync.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.sync.entity.CfRatingHistoryEntity;

import java.util.List;

@Mapper
public interface CfRatingHistoryMapper {

    int createTableIfNeeded();

    int deleteByUserId(@Param("userId") Long userId);

    int insert(CfRatingHistoryEntity entity);

    List<CfRatingHistoryEntity> selectByUserId(@Param("userId") Long userId);
}
