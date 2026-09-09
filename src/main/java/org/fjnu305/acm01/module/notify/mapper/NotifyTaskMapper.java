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

    List<NotifyTaskEntity> selectPendingBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    int markProcessing(@Param("ids") List<Long> ids);

    int reclaimStaleProcessing(@Param("staleSeconds") int staleSeconds);

    int releaseToPending(@Param("ids") List<Long> ids);

    int markSent(@Param("id") Long id);

    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    int markDead(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    int markDeadBatch(@Param("ids") List<Long> ids, @Param("errorMessage") String errorMessage);

    int incrementRetry(@Param("id") Long id, @Param("errorMessage") String errorMessage);

    int incrementRetryWithBackoff(@Param("id") Long id,
                                  @Param("errorMessage") String errorMessage,
                                  @Param("backoffSeconds") int backoffSeconds);

    int cancelBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    int cancelById(@Param("id") Long id);

    int updateScheduledAt(@Param("id") Long id, @Param("scheduledAt") LocalDateTime scheduledAt);

    int reactivate(@Param("id") Long id,
                   @Param("subscriptionId") Long subscriptionId,
                   @Param("scheduledAt") LocalDateTime scheduledAt);
}
