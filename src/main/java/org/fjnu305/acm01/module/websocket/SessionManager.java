package org.fjnu305.acm01.module.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {

    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(sessionId);
        log.debug("WebSocket session registered: userId={}, sessionId={}", userId, sessionId);
    }

    public void unregister(String sessionId) {
        userSessions.forEach((userId, sessions) -> {
            if (sessions.remove(sessionId) && sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        });
        log.debug("WebSocket session unregistered: sessionId={}", sessionId);
    }

    public boolean isUserOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    @EventListener
    public void onSessionConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = resolveUserId(accessor.getUser());
        String sessionId = accessor.getSessionId();
        if (userId != null && sessionId != null) {
            register(userId, sessionId);
        }
    }

    @EventListener
    public void onSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = resolveUserId(accessor.getUser());
        String sessionId = accessor.getSessionId();
        if (userId != null && sessionId != null) {
            register(userId, sessionId);
        }
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            unregister(sessionId);
        }
    }

    public static Long resolveUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object details = auth.getDetails();
            if (details instanceof Long userId) {
                return userId;
            }
            if (details instanceof Number number) {
                return number.longValue();
            }
        }
        if (principal instanceof StompUserPrincipal stompUser) {
            return stompUser.userId();
        }
        return null;
    }

    public record StompUserPrincipal(Long userId, String username) implements Principal {

        @Override
        public String getName() {
            return String.valueOf(userId);
        }
    }
}
