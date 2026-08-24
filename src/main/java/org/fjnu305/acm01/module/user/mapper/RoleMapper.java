package org.fjnu305.acm01.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.user.entity.RoleEntity;

import java.util.List;

@Mapper
public interface RoleMapper {

    RoleEntity selectByRoleCode(@Param("roleCode") String roleCode);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int countUsers();

    int deleteUserRoles(@Param("userId") Long userId);

    List<RoleEntity> selectAllActive();
}
