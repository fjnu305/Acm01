package org.fjnu305.acm01.module.solution;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.search.support.SearchDocumentFactory;
import org.fjnu305.acm01.module.solution.dto.SolutionUpdateRequest;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.service.SolutionAccessGuard;
import org.fjnu305.acm01.module.solution.service.SolutionCommandService;
import org.fjnu305.acm01.module.solution.service.SolutionQueryService;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

/**
 * 题解模块水平越权：不能修改/删除他人题解。
 */
@ExtendWith(MockitoExtension.class)
class SolutionServiceAccessTest {

    @Mock
    private SolutionMapper solutionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SearchDocumentFactory searchDocumentFactory;

    @Mock
    private SolutionAccessGuard solutionAccessGuard;

    @Mock
    private SolutionQueryService queryService;

    @InjectMocks
    private SolutionCommandService commandService;

    @Test
    void update_otherUsersSolution_throwsForbidden() {
        doThrow(new BusinessException(ErrorCode.SOLUTION_FORBIDDEN))
                .when(solutionAccessGuard).requireOwned(SecurityTestFixtures.USER_A_ID, 5L);

        SolutionUpdateRequest request = new SolutionUpdateRequest();
        request.setTitle("hacked");

        assertThatThrownBy(() ->
                commandService.update(SecurityTestFixtures.USER_A_ID, 5L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.SOLUTION_FORBIDDEN.getCode());
    }

    @Test
    void delete_otherUsersSolution_throwsForbidden() {
        doThrow(new BusinessException(ErrorCode.SOLUTION_FORBIDDEN))
                .when(solutionAccessGuard).requireOwned(SecurityTestFixtures.USER_A_ID, 6L);

        assertThatThrownBy(() ->
                commandService.delete(SecurityTestFixtures.USER_A_ID, 6L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.SOLUTION_FORBIDDEN.getCode());
    }
}
