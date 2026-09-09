package org.fjnu305.acm01.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.user.entity.UserEntity;

import java.util.List;

@Mapper
public interface UserMapper {

    UserEntity selectByUsername(@Param("username") String username);

    UserEntity selectById(@Param("id") Long id);

    List<UserEntity> selectByIds(@Param("ids") List<Long> ids);

    int countByUsername(@Param("username") String username);

    int insert(UserEntity user);

    int updateLastLoginTime(@Param("id") Long id);

    int updateProfile(UserEntity user);

    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    List<UserEntity> selectPage(@Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int countTodayRegistrations();

    int updateCfRating(@Param("id") Long id, @Param("cfRating") Integer cfRating);

    int updateCfHandle(@Param("id") Long id, @Param("cfHandle") String cfHandle);

    int updateContestCount(@Param("id") Long id, @Param("contestCount") int contestCount);

    List<Long> selectActiveUserIds();

    List<UserEntity> selectSearchCandidates(@Param("keyword") String keyword,
                                            @Param("excludeUserId") Long excludeUserId,
                                            @Param("limit") int limit);
}
