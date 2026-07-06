package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * AtCoder 赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>{@code https://atcoder.jp/contests/} 页面 HTML<br>
 * <b>实现方式：</b>Jsoup 解析表格 / 列表，提取赛事名、时间、链接。
 * </p>
 */
@Component
public class AtCoderCrawler extends AbstractContestCrawler {

    private static final String LIST_URL = "https://atcoder.jp/contests/";

    @Override
    public ContestSource getSource() {
        return ContestSource.ATCODER;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 2：Jsoup 解析 LIST_URL
        return Collections.emptyList();
    }
}
