package org.fjnu305.acm01.websocket;

import org.fjnu305.acm01.module.websocket.RedisWsPushBridge;
import org.fjnu305.acm01.module.websocket.SessionManager;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.fjnu305.acm01.module.websocket.service.RealtimePushServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisWsPushBridgeTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ObjectProvider<SimpMessagingTemplate> messagingTemplate;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private SimpMessagingTemplate template;
    @Mock
    private Message redisMessage;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RedisWsPushBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RedisWsPushBridge(stringRedisTemplate, objectMapper, messagingTemplate, sessionManager);
    }

    @Test
    void publish_sendsEnvelopeOnRedisChannel() {
        PushMessage payload = PushMessage.builder().title("t").type("contest").build();

        bridge.publish(9L, payload);

        verify(stringRedisTemplate).convertAndSend(eq(RedisWsPushBridge.CHANNEL), any(String.class));
        verify(template, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void onMessage_deliversOnlyWhenThisJvmOwnsSession() {
        when(messagingTemplate.getIfAvailable()).thenReturn(template);
        when(sessionManager.hasLocalSession(9L)).thenReturn(true);
        String json = objectMapper.writeValueAsString(envelope(9L, "hello"));
        when(redisMessage.getBody()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        bridge.onMessage(redisMessage, null);

        verify(template).convertAndSendToUser(eq("9"), eq(RealtimePushServiceImpl.NOTIFY_DESTINATION), any(PushMessage.class));
    }

    @Test
    void onMessage_skipsWhenSessionLivesOnAnotherInstance() {
        when(sessionManager.hasLocalSession(9L)).thenReturn(false);
        String json = objectMapper.writeValueAsString(envelope(9L, "hello"));
        when(redisMessage.getBody()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        bridge.onMessage(redisMessage, null);

        verify(template, never()).convertAndSendToUser(any(), any(), any());
    }

    private static RedisWsPushBridge.Envelope envelope(Long userId, String title) {
        RedisWsPushBridge.Envelope envelope = new RedisWsPushBridge.Envelope();
        envelope.setUserId(userId);
        envelope.setMessage(PushMessage.builder().title(title).build());
        return envelope;
    }
}
