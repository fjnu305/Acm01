package org.fjnu305.acm01.module.notify.delivery.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.delivery.service.NotifyDispatchService;
import org.fjnu305.acm01.module.notify.dto.NotifyDeliveryMessage;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notify.mq", name = "enabled", havingValue = "true")
public class NotifyMqConsumer {

    private final NotifyDispatchService notifyDispatchService;
    private final NotifyTaskMapper notifyTaskMapper;

    @RabbitListener(queues = "#{@notifyDeliveryQueue.name}")
    public void onMessage(NotifyDeliveryMessage message) {
        if (message == null || message.getTaskIds() == null || message.getTaskIds().isEmpty()) {
            return;
        }
        try {
            List<NotifyTaskEntity> tasks = new ArrayList<>();
            for (Long taskId : message.getTaskIds()) {
                NotifyTaskEntity task = notifyTaskMapper.selectById(taskId);
                if (task != null) {
                    tasks.add(task);
                }
            }
            if (!tasks.isEmpty()) {
                notifyDispatchService.deliver(message.getUserId(), tasks);
            }
        } catch (Exception e) {
            log.error("Notify consumer failed for user {}", message.getUserId(), e);
            throw e;
        }
    }
}
