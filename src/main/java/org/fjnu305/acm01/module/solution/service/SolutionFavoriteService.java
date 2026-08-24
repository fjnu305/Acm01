package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.util.PageParams;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionFavoriteMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolutionFavoriteService {

    private final SolutionMapper solutionMapper;
    private final SolutionFavoriteMapper solutionFavoriteMapper;
    private final SolutionAccessGuard solutionAccessGuard;

    @Transactional
    public void favorite(Long userId, Long solutionId) {
        SolutionEntity solution = solutionAccessGuard.requirePublished(solutionId);
        if (solutionFavoriteMapper.exists(userId, solutionId) > 0) {
            return;
        }
        solutionFavoriteMapper.insert(userId, solutionId);
        solutionMapper.updateFavoriteCount(solutionId, 1);
    }

    @Transactional
    public void unfavorite(Long userId, Long solutionId) {
        solutionAccessGuard.requirePublished(solutionId);
        int removed = solutionFavoriteMapper.delete(userId, solutionId);
        if (removed > 0) {
            solutionMapper.updateFavoriteCount(solutionId, -1);
        }
    }

    public PageResult<SolutionVO> listFavorites(Long userId, int pageNum, int pageSize) {
        PageParams page = PageParams.of(pageNum, pageSize);
        List<SolutionVO> list = solutionMapper.selectFavoritePage(userId, page.offset(), page.size());
        long total = solutionMapper.countFavoritePage(userId);
        return PageResult.of(list, total, page.page(), page.size());
    }
}
