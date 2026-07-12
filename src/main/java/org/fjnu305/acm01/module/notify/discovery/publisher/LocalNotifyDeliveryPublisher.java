package org.fjnu305.acm01.module.notify.discovery.publisher;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.notify.delivery.service.NotifyDispatchService;
import org.fjnu305.acm01.module.notify.discovery.api.NotifyDeliveryPublisher;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MQ 关闭时，发现侧直接调用执行侧（同进程兜底）。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notify.mq", name = "enabled", havingValue = "false")
public class LocalNotifyDeliveryPublisher implements NotifyDeliveryPublisher {

    private final NotifyDispatchService notifyDispatchService;

    @Override
    public void publish(Long userId, List<NotifyTaskEntity> tasks) {
        notifyDispatchService.deliver(userId, tasks);
    }
}
