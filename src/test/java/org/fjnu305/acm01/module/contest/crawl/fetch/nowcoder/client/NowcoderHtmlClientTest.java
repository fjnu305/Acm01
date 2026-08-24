package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.client;

import tools.jackson.databind.ObjectMapper;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto.NowcoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NowcoderHtmlClientTest {

    @Mock
    private CrawlHttpClient crawlHttpClient;
    @Mock
    private AppHttpProperties appHttpProperties;
    @Mock
    private NowcoderCrawlProperties nowcoderCrawlProperties;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NowcoderHtmlClient htmlClient;

    @Test
    void parse_shouldReadPlatformItemsFromDoubleEncodedDataJson() {
        when(nowcoderCrawlProperties.getListUrl())
                .thenReturn("https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13");
        when(crawlHttpClient.getHtml("https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13"))
                .thenReturn(DOUBLE_ENCODED_HTML);

        List<NowcoderContestItem> rows = htmlClient.fetch();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getId()).isEqualTo("137448");
        assertThat(rows.get(0).getTitle()).isEqualTo("牛客周赛 Round 152");
        assertThat(rows.get(0).getStartEpochSecond()).isEqualTo(1783854000L);
    }

    @Test
    void parse_shouldReadPlatformItemsFromDataJson() {
        when(nowcoderCrawlProperties.getListUrl())
                .thenReturn("https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13");
        when(crawlHttpClient.getHtml("https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13"))
                .thenReturn(SAMPLE_HTML);

        List<NowcoderContestItem> rows = htmlClient.fetch();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getId()).isEqualTo("137448");
        assertThat(rows.get(0).getTitle()).isEqualTo("牛客周赛 Round 152");
        assertThat(rows.get(0).getStartEpochSecond()).isEqualTo(1783854000L);
        assertThat(rows.get(0).getDurationSecond()).isEqualTo(7200L);
        assertThat(rows.get(0).getOrganizer()).isEqualTo("牛客竞赛");
        assertThat(rows.get(0).getRateChange()).isEqualTo("Rating＞1599");

        assertThat(rows.get(1).getId()).isEqualTo("137418");
        assertThat(rows.get(1).getTitle()).isEqualTo("牛客挑战赛90");
        assertThat(rows.get(1).getDurationSecond()).isEqualTo(10800L);
    }

    private static final String DOUBLE_ENCODED_HTML = """
            <html><body>
            <div data-id="137448" class="platform-item js-item " data-json="{&amp;quot;contestDuration&amp;quot;:7200000,&amp;quot;contestStartTime&amp;quot;:1783854000000,&amp;quot;contestName&amp;quot;:&amp;quot;牛客周赛 Round 152&amp;quot;,&amp;quot;contestId&amp;quot;:137448,&amp;quot;settingInfo&amp;quot;:{&amp;quot;organizerName&amp;quot;:&amp;quot;牛客竞赛&amp;quot;}}">
              <h4><a href="/acm/contest/137448">牛客周赛 Round 152</a></h4>
            </div>
            </body></html>
            """;

    private static final String SAMPLE_HTML = """
            <html><body>
            <div data-id="137448" class="platform-item js-item" data-json="{&quot;contestDuration&quot;:7200000,&quot;contestStartTime&quot;:1783854000000,&quot;contestName&quot;:&quot;牛客周赛 Round 152&quot;,&quot;contestId&quot;:137448,&quot;contestSignUpStartTime&quot;:1782871200000,&quot;contestSignUpEndTime&quot;:1783861200000,&quot;settingInfo&quot;:{&quot;organizerName&quot;:&quot;牛客竞赛&quot;}}">
              <h4><a href="/acm/contest/137448">牛客周赛 Round 152</a></h4>
              <ul class="platform-info">
                <li class="icon-nc-flash2">不计Rating的范围：Rating＞1599</li>
              </ul>
            </div>
            <div data-id="137418" class="platform-item js-item finish" data-json="{&quot;contestDuration&quot;:10800000,&quot;contestStartTime&quot;:1783681200000,&quot;contestName&quot;:&quot;牛客挑战赛90&quot;,&quot;contestId&quot;:137418,&quot;settingInfo&quot;:{&quot;organizerName&quot;:&quot;nowcoder.com&quot;}}">
              <h4><a href="/acm/contest/137418">牛客挑战赛90</a></h4>
            </div>
            </body></html>
            """;
}
