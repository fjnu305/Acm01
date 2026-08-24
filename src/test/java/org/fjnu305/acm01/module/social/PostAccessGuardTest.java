package org.fjnu305.acm01.module.social;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.social.entity.PostEntity;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.fjnu305.acm01.module.social.service.PostAccessGuard;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAccessGuardTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostAccessGuard postAccessGuard;

    @Test
    void requireOwnedActivePost_notOwner_throwsForbidden() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        post.setUserId(SecurityTestFixtures.USER_B_ID);
        post.setStatus(1);
        when(postMapper.selectById(1L)).thenReturn(post);

        assertThatThrownBy(() -> postAccessGuard.requireOwnedActivePost(SecurityTestFixtures.USER_A_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }
}
