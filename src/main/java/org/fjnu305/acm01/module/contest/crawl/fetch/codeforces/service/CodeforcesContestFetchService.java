package org.fjnu305.acm01.module.contest.crawl.fetch.codeforces;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.client.CodeforcesApiClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CodeforcesContestFetchService implements PlatformContestFetchService {

    private final CodeforcesApiClient apiClient;
    private final CodeforcesContestMapper contestMapper;

    @Override
    public ContestSource getSource() {
        return ContestSource.CODEFORCES;
    }

    @Override
    public String getPrimaryRequestUrl() {
        return CodeforcesApiClient.API_URL;
    }

    @Override
    public List<ContestDTO> fetchContests() {
        return contestMapper.mapItems(apiClient.fetchContests());
    }
}
