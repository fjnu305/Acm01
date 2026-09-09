package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.access.ContentAccessPolicy;
import org.fjnu305.acm01.Common.access.Viewer;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.util.PageParams;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTagMapper;
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
    private final SolutionTagMapper solutionTagMapper;
    private final ContentAccessPolicy contentAccessPolicy;

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
        Viewer viewer = Viewer.current();
        if (!viewer.authenticated() && viewerId != null) {
            viewer = Viewer.user(viewerId);
        }
        if (!contentAccessPolicy.canRead("solution", id, viewer)) {
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
        List<SolutionTagVO> rows = solutionTagMapper.selectCounts();
        if (rows == null || rows.isEmpty()) {
            return counts;
        }
        for (SolutionTagVO row : rows) {
            if (row.getName() == null || row.getName().isBlank() || row.getCount() == null) {
                continue;
            }
            counts.put(row.getName(), row.getCount());
        }
        return counts;
    }
}
