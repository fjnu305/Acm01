package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client;

import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.AtCoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto.AtCoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtCoderHtmlClientTest {

    @Mock
    private CrawlHttpClient crawlHttpClient;
    @Mock
    private AppHttpProperties appHttpProperties;
    @Mock
    private AtCoderCrawlProperties atCoderCrawlProperties;

    @InjectMocks
    private AtCoderHtmlClient htmlClient;

    @Test
    void parseTables_shouldFindTableInsidePanelWrapper() {
        when(atCoderCrawlProperties.getHtmlUrl()).thenReturn("https://atcoder.jp/contests/");
        when(crawlHttpClient.getHtml("https://atcoder.jp/contests/")).thenReturn(SAMPLE_HTML);

        List<AtCoderContestItem> rows = htmlClient.fetch();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getId()).isEqualTo("abc466");
        assertThat(rows.get(0).getTitle()).contains("AtCoder Beginner Contest 466");
        assertThat(rows.get(0).getRateChange()).isEqualTo("- 1999");
        assertThat(rows.get(0).getStartEpochSecond()).isNotNull();
        assertThat(rows.get(0).getDurationSecond()).isEqualTo(6000L);

        assertThat(rows.get(1).getId()).isEqualTo("arc224");
        assertThat(rows.get(1).getRateChange()).isEqualTo("800 - 2399");
        assertThat(rows.get(1).getDurationSecond()).isEqualTo(7200L);
    }

    private static final String SAMPLE_HTML = """
            <html><body>
            <h3>Ongoing Contests</h3>
            <div class="panel panel-default"><div class="table-responsive">
            <table class="table table-bordered">
              <tbody>
                <tr>
                  <td><time class="fixtime fixtime-full">2026-07-11 21:00:00+0900</time></td>
                  <td><a href="/contests/abc466">AtCoder Beginner Contest 466</a></td>
                  <td>01:40</td>
                  <td>- 1999</td>
                </tr>
              </tbody>
            </table>
            </div></div>
            <h3>Upcoming Contests</h3>
            <div class="panel panel-default"><div class="table-responsive">
            <table class="table table-bordered">
              <tbody>
                <tr>
                  <td><time class="fixtime fixtime-full">2026-07-12 21:00:00+0900</time></td>
                  <td><a href="/contests/arc224">AtCoder Regular Contest 224</a></td>
                  <td>02:00</td>
                  <td>800 - 2399</td>
                </tr>
              </tbody>
            </table>
            </div></div>
            </body></html>
            """;
}
