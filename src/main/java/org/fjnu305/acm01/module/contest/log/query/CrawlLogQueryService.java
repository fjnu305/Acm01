package org.fjnu305.acm01.module.contest.log.query;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.contest.log.entity.ContestCrawlLogEntity;
import org.fjnu305.acm01.module.contest.log.mapper.ContestCrawlLogMapper;
import org.fjnu305.acm01.module.contest.log.vo.CrawlLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlLogQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ContestCrawlLogMapper crawlLogMapper;

    public PageResult<CrawlLogVO> listLogs(String source, String status, int pageNum, int pageSize) {
        int safePageNum = pageNum > 0 ? pageNum : 1;
        int safePageSize = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : 20;
        int offset = (safePageNum - 1) * safePageSize;

        long total = crawlLogMapper.countPage(source, status);
        List<CrawlLogVO> list = crawlLogMapper.selectPage(source, status, offset, safePageSize)
                .stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(list, total, safePageNum, safePageSize);
    }

    private CrawlLogVO toVO(ContestCrawlLogEntity entity) {
        return CrawlLogVO.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .status(entity.getStatus())
                .triggerType(entity.getTriggerType())
                .errorType(entity.getErrorType())
                .httpStatus(entity.getHttpStatus())
                .requestUrl(entity.getRequestUrl())
                .fetchedCount(entity.getFetchedCount())
                .insertedCount(entity.getInsertedCount())
                .updatedCount(entity.getUpdatedCount())
                .skippedCount(entity.getSkippedCount())
                .ignoredCount(entity.getIgnoredCount())
                .elapsedMs(entity.getElapsedMs())
                .errorMessage(entity.getErrorMessage())
                .createdTime(entity.getCreatedTime())
                .build();
    }
}
