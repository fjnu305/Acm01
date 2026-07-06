package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ICPC 赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>ICPC 官网 / 区域赛公告页 HTML<br>
 * <b>特点：</b>赛事信息分散，需按区域赛页面分别抓取或聚合 RSS。
 * </p>
 */
@Component
public class IcpcCrawler extends AbstractContestCrawler {

    @Override
    public ContestSource getSource() {
        return ContestSource.ICPC;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 3：解析 ICPC 官网
        return Collections.emptyList();
    }
}
