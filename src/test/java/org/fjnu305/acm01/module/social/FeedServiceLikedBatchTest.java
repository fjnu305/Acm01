package org.fjnu305.acm01.module.social;

import org.fjnu305.acm01.module.social.config.SocialFeedProperties;
import org.fjnu305.acm01.module.social.mapper.LikeMapper;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.fjnu305.acm01.module.social.service.FeedService;
import org.fjnu305.acm01.module.social.vo.PostVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceLikedBatchTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SocialFeedProperties feedProperties;

    @InjectMocks
    private FeedService feedService;

    @Test
    void getHotFeed_batchesLikedLookup() {
        when(feedProperties.getHotFeedSize()).thenReturn(10);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);

        PostVO a = PostVO.builder().id(1L).likedByMe(false).build();
        PostVO b = PostVO.builder().id(2L).likedByMe(false).build();
        when(postMapper.selectHotFeed(0L, 10)).thenReturn(List.of(a, b));
        when(likeMapper.selectLikedPostIds(eq(9L), anyCollection())).thenReturn(List.of(2L));

        List<PostVO> feed = feedService.getHotFeed(9L);

        assertThat(feed.get(0).isLikedByMe()).isFalse();
        assertThat(feed.get(1).isLikedByMe()).isTrue();
        verify(likeMapper).selectLikedPostIds(eq(9L), anyCollection());
        verify(likeMapper, never()).exists(any(), any());
    }
}
