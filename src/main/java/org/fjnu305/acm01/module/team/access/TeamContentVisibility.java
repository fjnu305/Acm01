package org.fjnu305.acm01.module.team.access;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.access.ContentTypes;
import org.fjnu305.acm01.Common.access.ContentVisibilitySnapshot;
import org.fjnu305.acm01.Common.access.ContentVisibilitySource;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamContentVisibility implements ContentVisibilitySource {

    private final TeamPostMapper teamPostMapper;

    @Override
    public String type() {
        return ContentTypes.TEAM;
    }

    @Override
    public ContentVisibilitySnapshot load(Long refId) {
        TeamPostEntity entity = teamPostMapper.selectById(refId);
        if (entity == null) {
            return null;
        }
        return new ContentVisibilitySnapshot(entity.getUserId(), entity.getStatus(), entity.getDeleted());
    }
}
