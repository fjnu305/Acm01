package org.fjnu305.acm01.module.notify.writeTask.api;

import org.fjnu305.acm01.module.notify.writeTask.dto.NotifyScheduleCommand;

/**
 * subscription → notify 边界：登记 / 取消提醒任务。
 */
public interface NotifyTaskScheduler {

    void schedule(NotifyScheduleCommand command);

    void cancelBySubscriptionId(Long subscriptionId);
}
