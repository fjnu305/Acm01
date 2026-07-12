package org.fjnu305.acm01.module.contest.crawl.fetch;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;

import java.util.List;

public interface PlatformContestFetchService {

    ContestSource getSource();

    String getPrimaryRequestUrl();

    List<ContestDTO> fetchContests();
}
