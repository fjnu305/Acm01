package org.fjnu305.acm01.module.subscription.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.vo.SubscriptionVO;

import java.util.List;

/** 用户赛事订阅表数据访问 */
@Mapper
public interface ContestSubscriptionMapper {

    int insert(ContestSubscriptionEntity entity);

    ContestSubscriptionEntity selectById(@Param("id") Long id);

    ContestSubscriptionEntity selectActive(@Param("userId") Long userId,
                                           @Param("contestId") Long contestId,
                                           @Param("remindBeforeMinutes") int remindBeforeMinutes);

    ContestSubscriptionEntity selectAny(@Param("userId") Long userId,
                                        @Param("contestId") Long contestId,
                                        @Param("remindBeforeMinutes") int remindBeforeMinutes);

    int reactivate(@Param("id") Long id, @Param("channel") String channel);

    int cancel(@Param("id") Long id, @Param("userId") Long userId);

    List<SubscriptionVO> selectMyActive(@Param("userId") Long userId);
}
