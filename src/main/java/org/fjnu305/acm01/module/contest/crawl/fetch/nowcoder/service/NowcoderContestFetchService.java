package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderFetchCoordinator;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NowcoderContestFetchService implements PlatformContestFetchService {

    private final NowcoderFetchCoordinator fetchCoordinator;
    private final NowcoderCrawlProperties crawlProperties;

    @Override
    public ContestSource getSource() {
        return ContestSource.NOWCODER;
    }

    @Override
    public String getPrimaryRequestUrl() {
        return crawlProperties.getListUrl();
    }

    @Override
    public List<ContestDTO> fetchContests() {
        return fetchCoordinator.fetch();
    }
}
