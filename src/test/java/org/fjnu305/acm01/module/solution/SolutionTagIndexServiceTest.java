package org.fjnu305.acm01.module.solution;

import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTagMapper;
import org.fjnu305.acm01.module.solution.service.SolutionTagIndexService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SolutionTagIndexServiceTest {

    @Mock
    private SolutionTagMapper solutionTagMapper;
    @Mock
    private SolutionMapper solutionMapper;

    @InjectMocks
    private SolutionTagIndexService service;

    @Test
    void parseTags_splitsAndDedupes() {
        assertThat(SolutionTagIndexService.parseTags("DP, 图论，DP , "))
                .containsExactly("DP", "图论");
    }

    @Test
    void replace_unpublished_onlyDeletes() {
        service.replace(12L, "DP,图论", false);

        verify(solutionTagMapper).deleteBySolutionId(12L);
        verify(solutionTagMapper, never()).insert(12L, "DP");
    }

    @Test
    void replace_published_rewritesRows() {
        service.replace(12L, "DP,图论", true);

        verify(solutionTagMapper).deleteBySolutionId(12L);
        verify(solutionTagMapper).insert(12L, "DP");
        verify(solutionTagMapper).insert(12L, "图论");
    }
}
