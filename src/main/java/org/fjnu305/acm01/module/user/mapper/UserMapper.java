package org.fjnu305.acm01.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.user.entity.UserEntity;

@Mapper
public interface UserMapper {

    UserEntity selectByUsername(@Param("username") String username);

    UserEntity selectById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int insert(UserEntity user);

    int updateLastLoginTime(@Param("id") Long id);
}
