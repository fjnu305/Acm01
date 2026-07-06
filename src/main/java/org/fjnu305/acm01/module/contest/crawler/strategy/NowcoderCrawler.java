package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 牛客网赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>牛客竞赛列表页 HTML<br>
 * <b>实现方式：</b>Jsoup 解析，提取竞赛标题、时间、报名链接。
 * </p>
 */
@Component
public class NowcoderCrawler extends AbstractContestCrawler {

    @Override
    public ContestSource getSource() {
        return ContestSource.NOWCODER;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 2：解析牛客竞赛列表页
        return Collections.emptyList();
    }
}
