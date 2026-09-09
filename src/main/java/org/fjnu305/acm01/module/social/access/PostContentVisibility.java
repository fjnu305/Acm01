package org.fjnu305.acm01.module.social.access;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.access.ContentTypes;
import org.fjnu305.acm01.Common.access.ContentVisibilitySnapshot;
import org.fjnu305.acm01.Common.access.ContentVisibilitySource;
import org.fjnu305.acm01.module.social.entity.PostEntity;
import org.fjnu305.acm01.module.social.mapper.PostMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostContentVisibility implements ContentVisibilitySource {

    private final PostMapper postMapper;

    @Override
    public String type() {
        return ContentTypes.POST;
    }

    @Override
    public ContentVisibilitySnapshot load(Long refId) {
        PostEntity entity = postMapper.selectById(refId);
        if (entity == null) {
            return null;
        }
        return new ContentVisibilitySnapshot(entity.getUserId(), entity.getStatus(), 0);
    }
}
