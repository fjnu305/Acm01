package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AtCoder 爬虫配置。策略固定为 HTML 主源，失败降级 kenkoooo 接口。
 */
@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.atcoder")
public class AtCoderCrawlProperties {

    private String jsonFallbackUrl = "https://kenkoooo.com/atcoder/resources/contests.json";

    private String htmlUrl = "https://atcoder.jp/contests/";

    private int finishedKeepDays = 90;
}
