package org.fjnu305.acm01.module.websocket;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.fjnu305.acm01.module.websocket.service.RealtimePushServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * Fan-out STOMP user messages across app instances over Redis pub/sub.
 * The instance that owns the WebSocket session delivers locally.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisWsPushBridge implements MessageListener {

    public static final String CHANNEL = "acm:ws:push";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplate;
    private final SessionManager sessionManager;

    public void publish(Long userId, PushMessage message) {
        Envelope envelope = new Envelope();
        envelope.setUserId(userId);
        envelope.setMessage(message);
        try {
            stringRedisTemplate.convertAndSend(CHANNEL, objectMapper.writeValueAsString(envelope));
        } catch (Exception e) {
            log.warn("WS fanout publish failed, local fallback: {}", e.getMessage());
            deliverLocal(userId, message);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            Envelope envelope = objectMapper.readValue(json, Envelope.class);
            if (envelope == null || envelope.getUserId() == null || envelope.getMessage() == null) {
                return;
            }
            deliverLocal(envelope.getUserId(), envelope.getMessage());
        } catch (Exception e) {
            log.warn("WS fanout consume failed: {}", e.getMessage());
        }
    }

    private void deliverLocal(Long userId, PushMessage payload) {
        if (!sessionManager.hasLocalSession(userId)) {
            return;
        }
        SimpMessagingTemplate template = messagingTemplate.getIfAvailable();
        if (template == null) {
            return;
        }
        template.convertAndSendToUser(String.valueOf(userId), RealtimePushServiceImpl.NOTIFY_DESTINATION, payload);
        log.debug("WS delivered locally to user {}", userId);
    }

    @Data
    public static class Envelope {
        private Long userId;
        private PushMessage message;
    }
}
