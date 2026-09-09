package org.fjnu305.acm01.module.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cluster-wide online set. Local SessionManager still decides whether this JVM
 * holds the socket; this store answers "is the user connected anywhere".
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisWsPresenceStore {

    static final String ONLINE_KEY = "acm:ws:online:";
    static final String SESSION_KEY = "acm:ws:sess:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redis;

    public void addSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        try {
            String onlineKey = ONLINE_KEY + userId;
            redis.opsForSet().add(onlineKey, sessionId);
            redis.expire(onlineKey, TTL);
            redis.opsForValue().set(SESSION_KEY + sessionId, String.valueOf(userId), TTL);
        } catch (Exception e) {
            log.warn("WS presence add failed: {}", e.getMessage());
        }
    }

    public void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        try {
            String userId = redis.opsForValue().get(SESSION_KEY + sessionId);
            redis.delete(SESSION_KEY + sessionId);
            if (userId != null) {
                String onlineKey = ONLINE_KEY + userId;
                redis.opsForSet().remove(onlineKey, sessionId);
                Long remaining = redis.opsForSet().size(onlineKey);
                if (remaining == null || remaining == 0) {
                    redis.delete(onlineKey);
                }
            }
        } catch (Exception e) {
            log.warn("WS presence remove failed: {}", e.getMessage());
        }
    }

    public boolean isOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        try {
            Long size = redis.opsForSet().size(ONLINE_KEY + userId);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("WS presence check failed: {}", e.getMessage());
            return false;
        }
    }
}
