package org.fjnu305.acm01.module.contest.crawl.fetch.lanqiao;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LanqiaoContestFetchService implements PlatformContestFetchService {

    private static final String LIST_URL = "https://dasai.lanqiao.cn/";

    @Override
    public ContestSource getSource() {
        return ContestSource.LANQIAO;
    }

    @Override
    public String getPrimaryRequestUrl() {
        return LIST_URL;
    }

    @Override
    public List<ContestDTO> fetchContests() {
        return Collections.emptyList();
    }
}
