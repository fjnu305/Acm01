package org.fjnu305.acm01.Common.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Cross-instance mutex for Quartz jobs. {@code @DisallowConcurrentExecution} only
 * serializes within one JVM; Redis SET NX covers multiple replicas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJobLock {

    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @return true if this instance ran {@code body}
     */
    public boolean tryRun(String key, Duration ttl, Runnable body) {
        String token = UUID.randomUUID().toString();
        Boolean acquired;
        try {
            acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        } catch (Exception e) {
            log.error("Job lock Redis error, running {} locally", key, e);
            body.run();
            return true;
        }
        if (!Boolean.TRUE.equals(acquired)) {
            log.info("Skip job {}, lock held", key);
            return false;
        }
        try {
            body.run();
            return true;
        } finally {
            releaseIfOwner(key, token);
        }
    }

    private void releaseIfOwner(String key, String token) {
        try {
            stringRedisTemplate.execute(RELEASE, List.of(key), token);
        } catch (Exception e) {
            log.warn("Failed to release job lock {}", key, e);
        }
    }
}
