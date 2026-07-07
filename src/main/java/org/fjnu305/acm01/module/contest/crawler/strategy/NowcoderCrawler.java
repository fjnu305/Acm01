package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** 牛客网赛事爬虫策略（迭代 2） */
@Component
public class NowcoderCrawler extends AbstractContestCrawler {

    private static final String LIST_URL =
            "https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13";

    /** → contest.source = nowcoder */
    @Override
    public ContestSource getSource() {
        return ContestSource.NOWCODER;
    }

    /** → contest_crawl_log.request_url */
    @Override
    public String getPrimaryRequestUrl() {
        return LIST_URL;
    }

    /** TODO 迭代 2：解析 data-json → List&lt;ContestDTO&gt; */
    @Override
    protected List<ContestDTO> doFetch() {
        return Collections.emptyList();
    }
}
