package org.fjnu305.acm01.module.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.social.entity.TopicEntity;

import java.util.List;

@Mapper
public interface TopicMapper {

    TopicEntity selectById(@Param("id") Long id);

    List<TopicEntity> selectAll();

    int incrementPostCount(@Param("id") Long id);
}
