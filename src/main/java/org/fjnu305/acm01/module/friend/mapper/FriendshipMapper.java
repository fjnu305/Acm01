package org.fjnu305.acm01.module.friend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.friend.entity.FriendshipEntity;
import org.fjnu305.acm01.module.friend.vo.FriendVO;

import java.util.List;

@Mapper
public interface FriendshipMapper {

    int insert(FriendshipEntity entity);

    int deletePair(@Param("userId") Long userId, @Param("friendId") Long friendId);

    boolean exists(@Param("userId") Long userId, @Param("friendId") Long friendId);

    List<FriendVO> selectFriends(@Param("userId") Long userId,
                                 @Param("officialUserId") Long officialUserId);
}
