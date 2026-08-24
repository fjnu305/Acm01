package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.solution.dto.SolutionCreateRequest;
import org.fjnu305.acm01.module.solution.dto.SolutionUpdateRequest;
import org.fjnu305.acm01.module.solution.vo.SolutionDetailVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTagVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTemplateVO;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题解模块门面：委托 command / query / favorite，Controller 仅依赖此类。
 */
@Service
@RequiredArgsConstructor
public class SolutionService {

    private final SolutionCommandService commandService;
    private final SolutionQueryService queryService;
    private final SolutionFavoriteService favoriteService;

    public SolutionDetailVO create(Long userId, SolutionCreateRequest request) {
        return commandService.create(userId, request);
    }

    public SolutionDetailVO update(Long userId, Long id, SolutionUpdateRequest request) {
        return commandService.update(userId, id, request);
    }

    public void delete(Long userId, Long id) {
        commandService.delete(userId, id);
    }

    public void takedown(Long id) {
        commandService.takedown(id);
    }

    public void favorite(Long userId, Long solutionId) {
        favoriteService.favorite(userId, solutionId);
    }

    public void unfavorite(Long userId, Long solutionId) {
        favoriteService.unfavorite(userId, solutionId);
    }

    public PageResult<SolutionVO> listFavorites(Long userId, int pageNum, int pageSize) {
        return favoriteService.listFavorites(userId, pageNum, pageSize);
    }

    public PageResult<SolutionVO> list(String keyword, String tag, int pageNum, int pageSize) {
        return queryService.list(keyword, tag, pageNum, pageSize);
    }

    public SolutionDetailVO getDetail(Long id, Long viewerId) {
        return queryService.getDetail(id, viewerId);
    }

    public List<SolutionTemplateVO> listTemplates(String category) {
        return queryService.listTemplates(category);
    }

    public List<SolutionTagVO> listHotTags(int limit) {
        return queryService.listHotTags(limit);
    }

    public List<SolutionTagVO> listCategories() {
        return queryService.listCategories();
    }
}
