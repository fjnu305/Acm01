package org.fjnu305.acm01.websocket;

import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.module.websocket.StompAuthInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompAuthInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private StompAuthInterceptor interceptor;

    @Test
    void connectWithoutToken_throws() {
        Message<?> message = connectMessage(null);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT");
    }

    @Test
    void connectWithInvalidToken_throws() {
        when(jwtTokenProvider.resolveToken("Bearer bad")).thenReturn("bad");
        when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

        Message<?> message = connectMessage("Bearer bad");

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void connectWithValidToken_setsUser() {
        when(jwtTokenProvider.resolveToken("Bearer good")).thenReturn("good");
        when(jwtTokenProvider.validateToken("good")).thenReturn(true);
        when(jwtTokenProvider.getUserId("good")).thenReturn(1L);
        when(jwtTokenProvider.getUsername("good")).thenReturn("user_a");

        Message<?> message = connectMessage("Bearer good");
        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isNotNull();
    }

    private static Message<?> connectMessage(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
