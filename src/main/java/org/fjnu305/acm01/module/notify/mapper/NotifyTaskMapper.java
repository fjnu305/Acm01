package org.fjnu305.acm01.module.notify.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotifyTaskMapper {

    int insert(NotifyTaskEntity entity);

    NotifyTaskEntity selectById(@Param("id") Long id);

    NotifyTaskEntity selectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

    List<NotifyTaskEntity> selectDueTasks(@Param("limit") int limit);

    int markSent(@Param("id") Long id);

    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    int incrementRetry(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    int cancelBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    int reactivate(@Param("id") Long id,
                   @Param("subscriptionId") Long subscriptionId,
                   @Param("scheduledAt") LocalDateTime scheduledAt);
}
