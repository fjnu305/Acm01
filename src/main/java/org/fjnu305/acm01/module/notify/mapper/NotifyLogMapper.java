package org.fjnu305.acm01.module.notify.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.fjnu305.acm01.module.notify.entity.NotifyLogEntity;

@Mapper
public interface NotifyLogMapper {

    int insert(NotifyLogEntity entity);
}
