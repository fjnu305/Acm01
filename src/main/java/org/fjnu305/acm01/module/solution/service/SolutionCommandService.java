package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.search.support.SearchDocumentFactory;
import org.fjnu305.acm01.module.solution.dto.SolutionCreateRequest;
import org.fjnu305.acm01.module.solution.dto.SolutionUpdateRequest;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.util.MarkdownSanitizer;
import org.fjnu305.acm01.module.solution.vo.SolutionDetailVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolutionCommandService {

    private final SolutionMapper solutionMapper;
    private final UserMapper userMapper;
    private final SearchDocumentFactory searchDocumentFactory;
    private final SolutionAccessGuard solutionAccessGuard;
    private final SolutionQueryService queryService;
    private final SolutionTagIndexService solutionTagIndexService;

    @Transactional
    public SolutionDetailVO create(Long userId, SolutionCreateRequest request) {
        SolutionEntity entity = new SolutionEntity();
        entity.setUserId(userId);
        entity.setTitle(request.getTitle().trim());
        entity.setContent(MarkdownSanitizer.sanitize(request.getContent()));
        entity.setProblemSource(request.getProblemSource());
        entity.setProblemId(request.getProblemId());
        entity.setTags(normalizeTags(request.getTags()));
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        solutionMapper.insert(entity);
        syncSearch(entity);
        solutionTagIndexService.replace(entity.getId(), entity.getTags(), isPublished(entity.getStatus()));
        return queryService.getDetail(entity.getId(), userId);
    }

    @Transactional
    public SolutionDetailVO update(Long userId, Long id, SolutionUpdateRequest request) {
        SolutionEntity existing = solutionAccessGuard.requireOwned(userId, id);
        existing.setTitle(request.getTitle().trim());
        existing.setContent(MarkdownSanitizer.sanitize(request.getContent()));
        existing.setProblemSource(request.getProblemSource());
        existing.setProblemId(request.getProblemId());
        existing.setTags(normalizeTags(request.getTags()));
        existing.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        int updated = solutionMapper.update(existing);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.SOLUTION_FORBIDDEN);
        }
        syncSearch(existing);
        solutionTagIndexService.replace(existing.getId(), existing.getTags(), isPublished(existing.getStatus()));
        return queryService.getDetail(id, userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        solutionAccessGuard.requireOwned(userId, id);
        int deleted = solutionMapper.softDelete(id, userId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.SOLUTION_FORBIDDEN);
        }
        searchDocumentFactory.deleteSolution(id);
        solutionTagIndexService.remove(id);
    }

    @Transactional
    public void takedown(Long id) {
        SolutionEntity solution = solutionMapper.selectById(id);
        if (solution == null) {
            throw new BusinessException(ErrorCode.SOLUTION_NOT_FOUND);
        }
        solutionMapper.takedown(id);
        searchDocumentFactory.deleteSolution(id);
        solutionTagIndexService.remove(id);
    }

    private void syncSearch(SolutionEntity entity) {
        UserEntity author = userMapper.selectById(entity.getUserId());
        String authorName = author == null ? ""
                : (author.getNickname() != null ? author.getNickname() : author.getUsername());
        searchDocumentFactory.indexSolution(entity, authorName);
    }

    private static boolean isPublished(Integer status) {
        return status != null && status == 1;
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return tags.replace("，", ",").replace(" ", "");
    }
}
