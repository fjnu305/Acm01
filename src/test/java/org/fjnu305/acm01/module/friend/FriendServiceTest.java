package org.fjnu305.acm01.module.friend;

import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.friend.dto.SendFriendRequestDTO;
import org.fjnu305.acm01.module.friend.entity.FriendRequestEntity;
import org.fjnu305.acm01.module.friend.mapper.FriendRequestMapper;
import org.fjnu305.acm01.module.friend.mapper.FriendshipMapper;
import org.fjnu305.acm01.module.friend.service.FriendNotificationService;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.service.OfficialUserService;
import org.fjnu305.acm01.module.friend.vo.FriendRequestVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.service.UserService;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRequestMapper friendRequestMapper;
    @Mock
    private FriendshipMapper friendshipMapper;
    @Mock
    private UserService userService;
    @Mock
    private FriendNotificationService friendNotificationService;
    @Mock
    private OfficialUserService officialUserService;

    @InjectMocks
    private FriendService friendService;

    @Test
    void sendRequest_toSelf_throws() {
        SendFriendRequestDTO dto = new SendFriendRequestDTO();
        dto.setTargetUserId(SecurityTestFixtures.USER_A_ID);

        assertThatThrownBy(() -> friendService.sendRequest(SecurityTestFixtures.USER_A_ID, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.FRIEND_SELF.getCode());
    }

    @Test
    void sendRequest_alreadyFriends_throws() {
        SendFriendRequestDTO dto = new SendFriendRequestDTO();
        dto.setTargetUserId(SecurityTestFixtures.USER_B_ID);
        when(userService.requireActiveUser(SecurityTestFixtures.USER_A_ID)).thenReturn(user(SecurityTestFixtures.USER_A_ID));
        when(userService.requireActiveUser(SecurityTestFixtures.USER_B_ID)).thenReturn(user(SecurityTestFixtures.USER_B_ID));
        when(officialUserService.isOfficialUser(SecurityTestFixtures.USER_B_ID)).thenReturn(false);
        when(friendshipMapper.exists(SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_B_ID)).thenReturn(true);

        assertThatThrownBy(() -> friendService.sendRequest(SecurityTestFixtures.USER_A_ID, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(ErrorCode.ALREADY_FRIENDS.getCode());
    }

    @Test
    void sendRequest_success_notifiesAddressee() {
        SendFriendRequestDTO dto = new SendFriendRequestDTO();
        dto.setTargetUserId(SecurityTestFixtures.USER_B_ID);
        UserEntity requester = user(SecurityTestFixtures.USER_A_ID);
        when(userService.requireActiveUser(SecurityTestFixtures.USER_A_ID)).thenReturn(requester);
        when(userService.requireActiveUser(SecurityTestFixtures.USER_B_ID)).thenReturn(user(SecurityTestFixtures.USER_B_ID));
        when(officialUserService.isOfficialUser(SecurityTestFixtures.USER_B_ID)).thenReturn(false);
        when(friendshipMapper.exists(SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_B_ID)).thenReturn(false);
        when(friendRequestMapper.selectPendingBetween(SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_B_ID)).thenReturn(null);
        when(friendRequestMapper.selectByPair(SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_B_ID)).thenReturn(null);
        when(friendRequestMapper.selectVoById(any())).thenReturn(new FriendRequestVO());

        friendService.sendRequest(SecurityTestFixtures.USER_A_ID, dto);

        verify(friendNotificationService).notifyRequestSent(any(UserEntity.class), any(FriendRequestEntity.class));
    }

    private static UserEntity user(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
