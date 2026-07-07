package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** ICPC 赛事爬虫策略（迭代 3） */
@Component
public class IcpcCrawler extends AbstractContestCrawler {

    private static final String LIST_URL = "https://icpc.global/regionals/upcoming";

    /** → contest.source = icpc */
    @Override
    public ContestSource getSource() {
        return ContestSource.ICPC;
    }

    /** → contest_crawl_log.request_url */
    @Override
    public String getPrimaryRequestUrl() {
        return LIST_URL;
    }

    /** TODO 迭代 3：解析区域赛页面 → List&lt;ContestDTO&gt; */
    @Override
    protected List<ContestDTO> doFetch() {
        return Collections.emptyList();
    }
}
