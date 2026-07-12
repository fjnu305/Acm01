package org.fjnu305.acm01.module.notify.discovery.api;

import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;

import java.util.List;

/**
 * 任务发现 → 任务执行的边界：按用户批次投递待发送任务。
 */
public interface NotifyDeliveryPublisher {

    void publish(Long userId, List<NotifyTaskEntity> tasks);
}
