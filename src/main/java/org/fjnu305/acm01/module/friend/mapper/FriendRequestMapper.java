package org.fjnu305.acm01.module.friend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.friend.entity.FriendRequestEntity;
import org.fjnu305.acm01.module.friend.vo.FriendRequestVO;
import org.fjnu305.acm01.module.friend.vo.FriendVO;

import java.util.List;

@Mapper
public interface FriendRequestMapper {

    int insert(FriendRequestEntity entity);

    int updateStatus(@Param("id") Long id,
                     @Param("status") int status,
                     @Param("expectedStatus") Integer expectedStatus);

    int resend(@Param("id") Long id, @Param("message") String message);

    FriendRequestVO selectVoById(@Param("id") Long id);

    FriendRequestEntity selectById(@Param("id") Long id);

    FriendRequestEntity selectByPair(@Param("requesterId") Long requesterId,
                                     @Param("addresseeId") Long addresseeId);

    FriendRequestEntity selectPendingBetween(@Param("userIdA") Long userIdA,
                                             @Param("userIdB") Long userIdB);

    List<FriendRequestVO> selectIncoming(@Param("addresseeId") Long addresseeId);

    List<FriendRequestVO> selectOutgoing(@Param("requesterId") Long requesterId);
}
