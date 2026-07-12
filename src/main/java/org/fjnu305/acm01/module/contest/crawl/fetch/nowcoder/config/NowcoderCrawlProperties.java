package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.nowcoder")
public class NowcoderCrawlProperties {

    private String listUrl = "https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13";

    private int finishedKeepDays = 90;
}
