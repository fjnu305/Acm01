package org.fjnu305.acm01.boundary;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.contest.query.service.ContestQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContestQueryServiceBoundaryTest {

    @Mock
    private ContestQueryMapper contestQueryMapper;

    @Mock
    private org.fjnu305.acm01.module.contest.query.cache.ContestCacheService contestCacheService;

    @InjectMocks
    private ContestQueryService contestQueryService;

    private void stubCacheEmpty() {
        when(contestCacheService.getList(any(), any(), anyInt(), anyInt()))
                .thenReturn(java.util.Optional.empty());
    }

    @Test
    void listContests_clampsInvalidPageNumAndPageSize() {
        stubCacheEmpty();
        when(contestQueryMapper.countPage(null, null)).thenReturn(0L);
        when(contestQueryMapper.selectPage(null, null, 0, 100))
                .thenReturn(Collections.emptyList());

        var page = contestQueryService.listContests(null, null, 0, 999);

        assertThat(page.getPageNum()).isEqualTo(1L);
        assertThat(page.getPageSize()).isEqualTo(100L);
        verify(contestQueryMapper).selectPage(null, null, 0, 100);
    }

    @Test
    void getContestDetail_notFound_throws() {
        when(contestQueryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> contestQueryService.getContestDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.CONTEST_NOT_FOUND.getCode());
    }

    @Test
    void getContestDetail_found_returnsDetail() {
        ContestEntity entity = new ContestEntity();
        entity.setId(1L);
        entity.setTitle("Test");
        entity.setSource("codeforces");
        entity.setExternalId("1");
        when(contestQueryMapper.selectById(1L)).thenReturn(entity);

        var detail = contestQueryService.getContestDetail(1L);

        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getTitle()).isEqualTo("Test");
    }
}
