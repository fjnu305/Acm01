package org.fjnu305.acm01.module.social;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.social.mapper.LikeMapper;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.fjnu305.acm01.module.social.mapper.TopicMapper;
import org.fjnu305.acm01.module.social.service.FeedService;
import org.fjnu305.acm01.module.social.service.PostAccessGuard;
import org.fjnu305.acm01.module.social.service.PostService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

/**
 * 社交模块水平越权：不能删除他人动态。
 */
@ExtendWith(MockitoExtension.class)
class PostServiceAccessTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private FeedService feedService;

    @Mock
    private PostAccessGuard postAccessGuard;

    @InjectMocks
    private PostService postService;

    @Test
    void deleteOwnPost_otherUsersPost_throwsForbidden() {
        doThrow(new BusinessException(ErrorCode.FORBIDDEN))
                .when(postAccessGuard).requireOwnedActivePost(SecurityTestFixtures.USER_A_ID, 20L);

        assertThatThrownBy(() ->
                postService.deleteOwnPost(SecurityTestFixtures.USER_A_ID, 20L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }
}
