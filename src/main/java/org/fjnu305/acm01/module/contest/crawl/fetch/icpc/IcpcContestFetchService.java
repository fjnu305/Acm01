package org.fjnu305.acm01.module.contest.crawl.fetch.icpc;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class IcpcContestFetchService implements PlatformContestFetchService {

    private static final String LIST_URL = "https://icpc.global/regionals/upcoming";

    @Override
    public ContestSource getSource() {
        return ContestSource.ICPC;
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
