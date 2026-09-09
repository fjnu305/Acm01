package org.fjnu305.acm01.module.websocket;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnBean({StringRedisTemplate.class, RedisConnectionFactory.class, RedisWsPushBridge.class})
public class RedisWsListenerConfig {

    @Bean
    RedisMessageListenerContainer wsPushListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisWsPushBridge bridge) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(bridge, new ChannelTopic(RedisWsPushBridge.CHANNEL));
        return container;
    }
}
