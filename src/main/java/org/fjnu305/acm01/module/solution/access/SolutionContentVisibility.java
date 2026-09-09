package org.fjnu305.acm01.module.solution.access;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.access.ContentTypes;
import org.fjnu305.acm01.Common.access.ContentVisibilitySnapshot;
import org.fjnu305.acm01.Common.access.ContentVisibilitySource;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolutionContentVisibility implements ContentVisibilitySource {

    private final SolutionMapper solutionMapper;

    @Override
    public String type() {
        return ContentTypes.SOLUTION;
    }

    @Override
    public ContentVisibilitySnapshot load(Long refId) {
        SolutionEntity entity = solutionMapper.selectById(refId);
        if (entity == null) {
            return null;
        }
        return new ContentVisibilitySnapshot(entity.getUserId(), entity.getStatus(), entity.getDeleted());
    }
}
