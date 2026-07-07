package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** CCPC 赛事爬虫策略（迭代 3） */
@Component
public class CcpcCrawler extends AbstractContestCrawler {

    private static final String LIST_URL = "https://ccpc.io/placard";

    /** → contest.source = ccpc */
    @Override
    public ContestSource getSource() {
        return ContestSource.CCPC;
    }

    /** → contest_crawl_log.request_url */
    @Override
    public String getPrimaryRequestUrl() {
        return LIST_URL;
    }

    /** TODO 迭代 3：解析官网 HTML → List&lt;ContestDTO&gt; */
    @Override
    protected List<ContestDTO> doFetch() {
        return Collections.emptyList();
    }
}
