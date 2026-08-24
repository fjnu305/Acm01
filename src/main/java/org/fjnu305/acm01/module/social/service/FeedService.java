package org.fjnu305.acm01.module.social.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.social.config.SocialFeedProperties;
import org.fjnu305.acm01.module.social.mapper.LikeMapper;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.fjnu305.acm01.module.social.vo.PostVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedService {

    private static final String HOT_FEED_KEY = "social:feed:hot";

    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SocialFeedProperties feedProperties;

    public List<PostVO> getHotFeed(Long viewerId) {
        Optional<List<PostVO>> cached = getCachedHotFeed();
        if (cached.isPresent()) {
            return applyLikedByMe(cached.get(), viewerId);
        }

        List<PostVO> feed = postMapper.selectHotFeed(0L, feedProperties.getHotFeedSize());
        for (PostVO post : feed) {
            post.setLikedByMe(false);
        }
        putCachedHotFeed(feed);
        return applyLikedByMe(feed, viewerId);
    }

    public void evictHotFeed() {
        try {
            stringRedisTemplate.delete(HOT_FEED_KEY);
        } catch (Exception ignored) {
            // redis unavailable
        }
    }

    private Optional<List<PostVO>> getCachedHotFeed() {
        try {
            String json = stringRedisTemplate.opsForValue().get(HOT_FEED_KEY);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PostVO.class)));
        } catch (Exception e) {
            try {
                stringRedisTemplate.delete(HOT_FEED_KEY);
            } catch (Exception ignored) {
                // redis unavailable
            }
            return Optional.empty();
        }
    }

    private void putCachedHotFeed(List<PostVO> feed) {
        try {
            Duration ttl = Duration.ofMinutes(feedProperties.getCacheTtlMinutes());
            stringRedisTemplate.opsForValue().set(HOT_FEED_KEY, objectMapper.writeValueAsString(feed), ttl);
        } catch (Exception ignored) {
            // cache write failure should not break feed
        }
    }

    private List<PostVO> applyLikedByMe(List<PostVO> feed, Long viewerId) {
        if (viewerId == null || feed.isEmpty()) {
            return feed;
        }
        List<Long> postIds = feed.stream().map(PostVO::getId).toList();
        Set<Long> liked = new HashSet<>(likeMapper.selectLikedPostIds(viewerId, postIds));
        for (PostVO post : feed) {
            post.setLikedByMe(liked.contains(post.getId()));
        }
        return feed;
    }
}
