package org.fjnu305.acm01.module.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
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
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();
    private final ObjectProvider<RedisWsPresenceStore> presenceStore;

    public SessionManager(ObjectProvider<RedisWsPresenceStore> presenceStore) {
        this.presenceStore = presenceStore;
    }

    public void register(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUsers.put(sessionId, userId);
        RedisWsPresenceStore store = presenceStore.getIfAvailable();
        if (store != null) {
            store.addSession(userId, sessionId);
        }
        log.debug("WebSocket session registered: userId={}, sessionId={}", userId, sessionId);
    }

    public void unregister(String sessionId) {
        Long userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        RedisWsPresenceStore store = presenceStore.getIfAvailable();
        if (store != null) {
            store.removeSession(sessionId);
        }
        log.debug("WebSocket session unregistered: sessionId={}", sessionId);
    }

    public boolean hasLocalSession(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public boolean isUserOnline(Long userId) {
        if (hasLocalSession(userId)) {
            return true;
        }
        RedisWsPresenceStore store = presenceStore.getIfAvailable();
        return store != null && store.isOnline(userId);
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
