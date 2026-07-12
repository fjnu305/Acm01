package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.mapper;

import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderContestMapper;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto.NowcoderContestItem;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NowcoderContestMapperTest {

    private NowcoderContestMapper mapper;

    @BeforeEach
    void setUp() {
        NowcoderCrawlProperties properties = new NowcoderCrawlProperties();
        properties.setFinishedKeepDays(90);
        mapper = new NowcoderContestMapper(properties);
    }

    @Test
    void mapItem_shouldInvertExcludedRatingUpperBound() {
        NowcoderContestItem item = baseItem();
        item.setRateChange("Rating＞1599");

        ContestDTO dto = mapper.mapItem(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getDifficulty()).isEqualTo("Rating≤1599");
    }

    @Test
    void mapItem_shouldInvertExcludedRatingLowerBound() {
        NowcoderContestItem item = baseItem();
        item.setRateChange("Rating＜1200");

        ContestDTO dto = mapper.mapItem(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getDifficulty()).isEqualTo("Rating≥1200");
    }

    private NowcoderContestItem baseItem() {
        NowcoderContestItem item = new NowcoderContestItem();
        item.setId("137448");
        item.setTitle("牛客周赛 Round 152");
        item.setStartEpochSecond(1_783_854_000L);
        item.setDurationSecond(7200L);
        return item;
    }
}
