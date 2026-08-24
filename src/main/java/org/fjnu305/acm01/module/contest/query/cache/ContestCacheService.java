package org.fjnu305.acm01.module.contest.query.cache;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.contest.vo.ContestVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContestCacheService {

    private static final String LIST_KEY_PREFIX = "contest:list:";
    private static final Duration LIST_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<PageResult<ContestVO>> getList(String source, Integer status, int pageNum, int pageSize) {
        String key = listKey(source, status, pageNum, pageSize);
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(PageResult.class, ContestVO.class);
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception e) {
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception ignored) {
                // redis unavailable
            }
            return Optional.empty();
        }
    }

    public void putList(String source, Integer status, int pageNum, int pageSize, PageResult<ContestVO> page) {
        String key = listKey(source, status, pageNum, pageSize);
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(page), LIST_TTL);
        } catch (Exception ignored) {
            // cache write failure should not break query
        }
    }

    public void evictAllLists() {
        try {
            var keys = stringRedisTemplate.keys(LIST_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
            // redis unavailable
        }
    }

    private static String listKey(String source, Integer status, int pageNum, int pageSize) {
        return LIST_KEY_PREFIX
                + (source != null ? source : "all")
                + ":" + (status != null ? status : "all")
                + ":" + pageNum
                + ":" + pageSize;
    }
}
