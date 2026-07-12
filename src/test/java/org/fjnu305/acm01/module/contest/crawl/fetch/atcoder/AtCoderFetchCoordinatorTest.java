package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderHtmlClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderJsonClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto.AtCoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtCoderFetchCoordinatorTest {

    @Mock
    private AtCoderJsonClient jsonClient;
    @Mock
    private AtCoderHtmlClient htmlClient;
    @Mock
    private AtCoderContestMapper contestMapper;

    @InjectMocks
    private AtCoderFetchCoordinator coordinator;

    @Test
    void shouldUseHtmlWhenCrawlSucceeds() {
        when(htmlClient.fetch()).thenReturn(List.of(sampleItem("abc466")));
        when(contestMapper.mapItems(anyList())).thenReturn(List.of(sampleDto("abc466")));

        List<ContestDTO> result = coordinator.fetch();

        assertThat(result).extracting(ContestDTO::getExternalId).containsExactly("abc466");
        verifyNoInteractions(jsonClient);
    }

    @Test
    void shouldKeepEmptyHtmlResultWithoutFallback() {
        when(htmlClient.fetch()).thenReturn(List.of());
        when(contestMapper.mapItems(anyList())).thenReturn(List.of());

        List<ContestDTO> result = coordinator.fetch();

        assertThat(result).isEmpty();
        verifyNoInteractions(jsonClient);
    }

    @Test
    void shouldFallbackToJsonWhenHtmlFails() {
        when(htmlClient.fetch()).thenThrow(
                CrawlFetchException.unknown("https://atcoder.jp/contests/", 3, "boom", null));
        when(jsonClient.fetch()).thenReturn(List.of());
        when(contestMapper.mapItems(anyList())).thenReturn(List.of(sampleDto("abc465")));

        List<ContestDTO> result = coordinator.fetch();

        assertThat(result).extracting(ContestDTO::getExternalId).containsExactly("abc465");
        verify(jsonClient).fetch();
    }

    private ContestDTO sampleDto(String externalId) {
        return ContestDTO.builder()
                .source(ContestSource.ATCODER)
                .externalId(externalId)
                .title("contest-" + externalId)
                .startTime(LocalDateTime.of(2026, 7, 11, 21, 0))
                .build();
    }

    private AtCoderContestItem sampleItem(String externalId) {
        AtCoderContestItem item = new AtCoderContestItem();
        item.setId(externalId);
        item.setTitle("contest-" + externalId);
        item.setStartEpochSecond(LocalDateTime.of(2026, 7, 11, 21, 0)
                .atZone(ZoneId.of("Asia/Tokyo")).toEpochSecond());
        item.setDurationSecond(6000L);
        item.setRateChange("- 1999");
        return item;
    }
}
