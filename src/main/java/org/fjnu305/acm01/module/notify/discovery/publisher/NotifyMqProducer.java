package org.fjnu305.acm01.module.notify.discovery.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.discovery.api.NotifyDeliveryPublisher;
import org.fjnu305.acm01.module.notify.dto.NotifyDeliveryMessage;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notify.mq", name = "enabled", havingValue = "true")
public class NotifyMqProducer implements NotifyDeliveryPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotifyProperties properties;

    @Override
    public void publish(Long userId, List<NotifyTaskEntity> tasks) {
        List<Long> taskIds = tasks.stream().map(NotifyTaskEntity::getId).toList();
        NotifyDeliveryMessage message = new NotifyDeliveryMessage(userId, taskIds);
        rabbitTemplate.convertAndSend(
                properties.getMq().getExchange(),
                properties.getMq().getRoutingKey(),
                message);
        log.info("Published notify delivery for user {} with {} task(s)", userId, taskIds.size());
    }
}
