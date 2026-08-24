package org.fjnu305.acm01.module.solution;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.service.SolutionAccessGuard;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionAccessGuardTest {

    @Mock
    private SolutionMapper solutionMapper;

    @InjectMocks
    private SolutionAccessGuard solutionAccessGuard;

    @Test
    void requirePublished_draft_throwsNotFound() {
        SolutionEntity entity = new SolutionEntity();
        entity.setId(1L);
        entity.setStatus(0);
        when(solutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> solutionAccessGuard.requirePublished(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.SOLUTION_NOT_FOUND.getCode());
    }

    @Test
    void requireOwned_wrongUser_throwsForbidden() {
        SolutionEntity entity = new SolutionEntity();
        entity.setId(1L);
        entity.setUserId(SecurityTestFixtures.USER_B_ID);
        when(solutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> solutionAccessGuard.requireOwned(SecurityTestFixtures.USER_A_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.SOLUTION_FORBIDDEN.getCode());
    }
}
