package org.fjnu305.acm01.module.contest.crawl.common.rate;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.mapper.ContestSourceMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Per-platform Redis rate limiting using contest_source.rate_limit_sec. */
@Component
@RequiredArgsConstructor
public class CrawlRateLimiter {

    private static final String KEY_PREFIX = "crawl:rate:";
    private static final int DEFAULT_INTERVAL_SEC = 3;

    private final StringRedisTemplate stringRedisTemplate;
    private final ContestSourceMapper contestSourceMapper;

    public void acquire(ContestSource source) {
        int intervalSec = resolveInterval(source);
        String key = KEY_PREFIX + source.getValue();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(intervalSec));
        if (Boolean.TRUE.equals(acquired)) {
            return;
        }
        long waitMs = intervalSec * 1000L;
        try {
            TimeUnit.MILLISECONDS.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stringRedisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(intervalSec));
    }

    private int resolveInterval(ContestSource source) {
        Integer sec = contestSourceMapper.selectRateLimitSec(source.getValue());
        return sec != null && sec > 0 ? sec : DEFAULT_INTERVAL_SEC;
    }
}
