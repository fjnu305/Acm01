package org.fjnu305.acm01.module.solution;

import org.fjnu305.acm01.Common.access.ContentAccessPolicy;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTagMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTemplateMapper;
import org.fjnu305.acm01.module.solution.service.SolutionQueryService;
import org.fjnu305.acm01.module.solution.vo.SolutionTagVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionQueryServiceHotTagsTest {

    @Mock
    private SolutionMapper solutionMapper;
    @Mock
    private SolutionTemplateMapper solutionTemplateMapper;
    @Mock
    private SolutionTagMapper solutionTagMapper;
    @Mock
    private ContentAccessPolicy contentAccessPolicy;

    @InjectMocks
    private SolutionQueryService queryService;

    @Test
    void listHotTags_usesInvertedIndexNotFullScan() {
        when(solutionTagMapper.selectCounts()).thenReturn(List.of(
                new SolutionTagVO("DP", 5),
                new SolutionTagVO("图论", 2)
        ));

        List<SolutionTagVO> tags = queryService.listHotTags(8);

        assertThat(tags).extracting(SolutionTagVO::getName).containsExactly("DP", "图论");
        verify(solutionMapper, never()).selectAllTags();
    }

    @Test
    void listCategories_readsCountsFromIndex() {
        when(solutionTagMapper.selectCounts()).thenReturn(List.of(new SolutionTagVO("DP", 4)));

        List<SolutionTagVO> categories = queryService.listCategories();

        assertThat(categories).anySatisfy(tag -> {
            assertThat(tag.getName()).isEqualTo("DP");
            assertThat(tag.getCount()).isEqualTo(4);
        });
        verify(solutionMapper, never()).selectAllTags();
    }
}
