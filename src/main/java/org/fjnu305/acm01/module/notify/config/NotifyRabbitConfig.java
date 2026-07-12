package org.fjnu305.acm01.module.notify.config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.notify.dto.NotifyDeliveryMessage;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notify.mq", name = "enabled", havingValue = "true")
public class NotifyRabbitConfig {

    private final NotifyProperties notifyProperties;

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(notifyProperties.getMq().getExchange(), true, false);
    }

    @Bean
    public Queue notifyDeliveryQueue() {
        return new Queue(notifyProperties.getMq().getQueue(), true);
    }

    @Bean
    public Binding notifyDeliveryBinding(Queue notifyDeliveryQueue, DirectExchange notifyExchange) {
        return BindingBuilder.bind(notifyDeliveryQueue)
                .to(notifyExchange)
                .with(notifyProperties.getMq().getRoutingKey());
    }

    @Bean
    public MessageConverter notifyMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate notifyRabbitTemplate(ConnectionFactory connectionFactory,
                                             MessageConverter notifyMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(notifyMessageConverter);
        return template;
    }
}
