package org.fjnu305.acm01.module.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or missing JWT for WebSocket CONNECT");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String username = jwtTokenProvider.getUsername(token);
        SessionManager.StompUserPrincipal principal = new SessionManager.StompUserPrincipal(userId, username);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(userId);
        accessor.setUser(authentication);
        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return jwtTokenProvider.resolveToken(authHeaders.get(0));
        }
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }
        return null;
    }
}
