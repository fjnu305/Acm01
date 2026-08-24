package org.fjnu305.acm01.module.subscription;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.notify.writeTask.api.NotifyTaskScheduler;
import org.fjnu305.acm01.module.subscription.config.SubscriptionProperties;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.fjnu305.acm01.module.subscription.service.SubscriptionService;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Test helper. */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceAccessTest {

    @Mock
    private ContestSubscriptionMapper subscriptionMapper;

    @Mock
    private ContestQueryMapper contestQueryMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotifyTaskScheduler notifyTaskScheduler;

    @Mock
    private SubscriptionProperties subscriptionProperties;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void cancel_otherUsersSubscription_throwsNotFound() {
        ContestSubscriptionEntity subscription = new ContestSubscriptionEntity();
        subscription.setId(10L);
        subscription.setUserId(SecurityTestFixtures.USER_B_ID);
        subscription.setStatus(1);
        when(subscriptionMapper.selectById(10L)).thenReturn(subscription);

        assertThatThrownBy(() ->
                subscriptionService.cancel(SecurityTestFixtures.USER_A_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.SUBSCRIPTION_NOT_FOUND.getCode());
    }
}
