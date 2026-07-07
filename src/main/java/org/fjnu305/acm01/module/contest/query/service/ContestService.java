package org.fjnu305.acm01.module.contest.query.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.contest.dto.ContestQueryRequest;
import org.fjnu305.acm01.module.contest.dto.ContestVO;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.mapper.ContestMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 赛事查询服务：分页列表、详情、日历、热门。
 */
@Service
@RequiredArgsConstructor
public class ContestService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ContestMapper contestMapper;

    /**
     * 分页查询赛事列表，默认按开始时间倒序。
     */
    public PageResult<ContestVO> listContests(ContestQueryRequest request) {
        int pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? Math.min(request.getPageSize(), MAX_PAGE_SIZE) : 20;
        int offset = (pageNum - 1) * pageSize;

        long total = contestMapper.countPage(request.getSource(), request.getStatus());
        List<ContestVO> list = contestMapper.selectPage(
                request.getSource(), request.getStatus(), offset, pageSize
        ).stream().map(this::toVO).toList();

        return PageResult.of(list, total, pageNum, pageSize);
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
}
