package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.client.NowcoderHtmlClient;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NowcoderFetchCoordinator {

    private final NowcoderHtmlClient htmlClient;
    private final NowcoderContestMapper contestMapper;

    public List<ContestDTO> fetch() {
        List<ContestDTO> contests = contestMapper.mapItems(htmlClient.fetch());
        log.info("[nowcoder] fetched={}", contests.size());
        return contests;
    }
}
