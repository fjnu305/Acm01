package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AtCoderContestFetchService implements PlatformContestFetchService {

    private final AtCoderFetchCoordinator fetchCoordinator;
    private final AtCoderCrawlProperties crawlProperties;

    @Override
    public ContestSource getSource() {
        return ContestSource.ATCODER;
    }

    @Override
    public String getPrimaryRequestUrl() {
        return crawlProperties.getHtmlUrl();
    }

    @Override
    public List<ContestDTO> fetchContests() {
        return fetchCoordinator.fetch();
    }
}
