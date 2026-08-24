package org.fjnu305.acm01.module.contest.query.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.cache.ContestCacheService;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.contest.vo.ContestDetailVO;
import org.fjnu305.acm01.module.contest.vo.ContestVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ContestQueryMapper contestQueryMapper;
    private final ContestCacheService contestCacheService;

    public PageResult<ContestVO> listContests(String source, Integer status, int pageNum, int pageSize) {
        int safePageNum = pageNum > 0 ? pageNum : 1;
        int safePageSize = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : 20;

        return contestCacheService.getList(source, status, safePageNum, safePageSize)
                .orElseGet(() -> {
                    int offset = (safePageNum - 1) * safePageSize;
                    long total = contestQueryMapper.countPage(source, status);
                    List<ContestVO> list = contestQueryMapper.selectPage(
                            source, status, offset, safePageSize
                    ).stream().map(this::toVO).toList();
                    PageResult<ContestVO> page = PageResult.of(list, total, safePageNum, safePageSize);
                    contestCacheService.putList(source, status, safePageNum, safePageSize, page);
                    return page;
                });
    }

    public ContestDetailVO getContestDetail(Long id) {
        ContestEntity entity = contestQueryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.CONTEST_NOT_FOUND);
        }
        return toDetailVO(entity);
    }

    private ContestVO toVO(ContestEntity entity) {
        return ContestVO.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .externalId(entity.getExternalId())
                .title(entity.getTitle())
                .url(entity.getUrl())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .difficulty(entity.getDifficulty())
                .contestType(entity.getContestType())
                .location(entity.getLocation())
                .build();
    }

    private ContestDetailVO toDetailVO(ContestEntity entity) {
        return ContestDetailVO.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .externalId(entity.getExternalId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .registerStart(entity.getRegisterStart())
                .registerEnd(entity.getRegisterEnd())
                .status(entity.getStatus())
                .difficulty(entity.getDifficulty())
                .contestType(entity.getContestType())
                .location(entity.getLocation())
                .lastCrawledAt(entity.getLastCrawledAt())
                .build();
    }
}
