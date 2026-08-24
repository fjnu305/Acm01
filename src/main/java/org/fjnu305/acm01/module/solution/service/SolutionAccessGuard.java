package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolutionAccessGuard {

    private final SolutionMapper solutionMapper;

    public SolutionEntity requireOwned(Long userId, Long id) {
        SolutionEntity entity = solutionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.SOLUTION_NOT_FOUND);
        }
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SOLUTION_FORBIDDEN);
        }
        return entity;
    }

    public SolutionEntity requirePublished(Long id) {
        SolutionEntity entity = solutionMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException(ErrorCode.SOLUTION_NOT_FOUND);
        }
        return entity;
    }
}
