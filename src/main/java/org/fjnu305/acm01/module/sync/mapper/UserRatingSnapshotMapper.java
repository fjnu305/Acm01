package org.fjnu305.acm01.module.sync.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.sync.entity.UserRatingSnapshotEntity;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserRatingSnapshotMapper {

    int upsert(UserRatingSnapshotEntity entity);

    List<UserRatingSnapshotEntity> selectByUserId(@Param("userId") Long userId);

    List<UserRatingSnapshotEntity> selectRecentByUserAndPlatform(
            @Param("userId") Long userId,
            @Param("platform") String platform,
            @Param("limit") int limit);

    UserRatingSnapshotEntity selectLatestByUserAndPlatform(
            @Param("userId") Long userId,
            @Param("platform") String platform);

    UserRatingSnapshotEntity selectByUserPlatformDate(
            @Param("userId") Long userId,
            @Param("platform") String platform,
            @Param("snapshotDate") LocalDate snapshotDate);
}
