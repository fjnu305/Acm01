package org.fjnu305.acm01.module.contest.crawl.fetch.luogu;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LuoguContestFetchService implements PlatformContestFetchService {

    private static final String LIST_URL =
            "https://www.luogu.com.cn/contest/list?_contentOnly=1";

    @Override
    public ContestSource getSource() {
        return ContestSource.LUOGU;
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
