package org.fjnu305.acm01.module.sync.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.sync.entity.OjAccountEntity;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OjAccountMapper {

    int insert(OjAccountEntity entity);

    int update(OjAccountEntity entity);

    OjAccountEntity selectByUserAndPlatform(@Param("userId") Long userId, @Param("platform") String platform);

    List<OjAccountEntity> selectByUserId(@Param("userId") Long userId);

    List<OjAccountEntity> selectAllActive();

    int updateLastSyncAt(@Param("id") Long id, @Param("lastSyncAt") LocalDateTime lastSyncAt);
}
