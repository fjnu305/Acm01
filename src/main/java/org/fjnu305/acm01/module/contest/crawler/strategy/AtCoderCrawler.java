package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * AtCoder 赛事爬虫策略（迭代 2）。
 * <p>输出 {@link ContestDTO}，字段对标 contest 表 §3.1。</p>
 */
@Component
public class AtCoderCrawler extends AbstractContestCrawler {

    private static final String LIST_URL = "https://atcoder.jp/contests/";

    /** → contest.source = atcoder */
    @Override
    public ContestSource getSource() {
        return ContestSource.ATCODER;
    }

    /** → contest_crawl_log.request_url */
    @Override
    public String getPrimaryRequestUrl() {
        return LIST_URL;
    }

    /** TODO 迭代 2：解析 HTML → List&lt;ContestDTO&gt; */
    @Override
    protected List<ContestDTO> doFetch() {
        return Collections.emptyList();
    }
}
