package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.util.PageParams;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTemplateMapper;
import org.fjnu305.acm01.module.solution.vo.SolutionDetailVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTagVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTemplateVO;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolutionQueryService {

    private static final List<String> CATEGORY_TAGS = List.of(
            "DP", "图论", "数据结构", "数学", "字符串", "贪心",
            "二分", "搜索", "模拟", "数论", "几何", "并查集"
    );

    private final SolutionMapper solutionMapper;
    private final SolutionTemplateMapper solutionTemplateMapper;

    public PageResult<SolutionVO> list(String keyword, String tag, int pageNum, int pageSize) {
        PageParams page = PageParams.of(pageNum, pageSize);
        List<SolutionVO> list = solutionMapper.selectPage(keyword, tag, page.offset(), page.size());
        long total = solutionMapper.countPage(keyword, tag);
        return PageResult.of(list, total, page.page(), page.size());
    }

    public SolutionDetailVO getDetail(Long id, Long viewerId) {
        SolutionDetailVO detail = solutionMapper.selectDetail(id, viewerId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.SOLUTION_NOT_FOUND);
        }
        if (detail.getStatus() != null && detail.getStatus() == 2) {
            throw new BusinessException(ErrorCode.SOLUTION_TAKEDOWN);
        }
        if (detail.getStatus() != null && detail.getStatus() != 1
                && (viewerId == null || !detail.getUserId().equals(viewerId))) {
            throw new BusinessException(ErrorCode.SOLUTION_NOT_FOUND);
        }
        solutionMapper.incrementViewCount(id);
        return detail;
    }

    public List<SolutionTemplateVO> listTemplates(String category) {
        return solutionTemplateMapper.selectAll(category);
    }

    public List<SolutionTagVO> listHotTags(int limit) {
        Map<String, Integer> counts = aggregateTagCounts();
        int size = limit <= 0 ? 8 : Math.min(limit, 20);
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(size)
                .map(entry -> new SolutionTagVO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SolutionTagVO> listCategories() {
        Map<String, Integer> counts = aggregateTagCounts();
        List<SolutionTagVO> categories = new ArrayList<>();
        for (String tag : CATEGORY_TAGS) {
            categories.add(new SolutionTagVO(tag, counts.getOrDefault(tag, 0)));
        }
        return categories;
    }

    private Map<String, Integer> aggregateTagCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String row : solutionMapper.selectAllTags()) {
            if (row == null || row.isBlank()) {
                continue;
            }
            for (String part : row.split("[,，]")) {
                String tag = part.trim();
                if (!tag.isEmpty()) {
                    counts.merge(tag, 1, Integer::sum);
                }
            }
        }
        return counts;
    }
}
