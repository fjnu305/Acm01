package org.fjnu305.acm01.module.friend.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.util.TextUtils;
import org.fjnu305.acm01.module.friend.constant.FriendRequestStatus;
import org.fjnu305.acm01.module.friend.constant.FriendStatus;
import org.fjnu305.acm01.module.friend.dto.SendFriendRequestDTO;
import org.fjnu305.acm01.module.friend.entity.FriendRequestEntity;
import org.fjnu305.acm01.module.friend.mapper.FriendRequestMapper;
import org.fjnu305.acm01.module.friend.mapper.FriendshipMapper;
import org.fjnu305.acm01.module.friend.support.FriendshipPairs;
import org.fjnu305.acm01.module.friend.vo.FriendRequestVO;
import org.fjnu305.acm01.module.friend.vo.FriendVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int FRIEND_REQUEST_MESSAGE_MAX = 200;

    private final FriendRequestMapper friendRequestMapper;
    private final FriendshipMapper friendshipMapper;
    private final UserService userService;
    private final FriendNotificationService friendNotificationService;
    private final OfficialUserService officialUserService;

    public List<FriendVO> listFriends(Long userId) {
        return friendshipMapper.selectFriends(userId, officialUserService.requireOfficialUserId());
    }

    public List<FriendRequestVO> listIncoming(Long userId) {
        return friendRequestMapper.selectIncoming(userId);
    }

    public List<FriendRequestVO> listOutgoing(Long userId) {
        return friendRequestMapper.selectOutgoing(userId);
    }

    @Transactional
    public FriendRequestVO sendRequest(Long requesterId, SendFriendRequestDTO request) {
        Long addresseeId = request.getTargetUserId();
        if (addresseeId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "targetUserId is required");
        }
        if (requesterId.equals(addresseeId)) {
            throw new BusinessException(ErrorCode.FRIEND_SELF);
        }
        if (officialUserService.isOfficialUser(addresseeId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无法向官方账号发送好友申请");
        }
        UserEntity requester = userService.requireActiveUser(requesterId);
        userService.requireActiveUser(addresseeId);

        if (friendshipMapper.exists(requesterId, addresseeId)) {
            throw new BusinessException(ErrorCode.ALREADY_FRIENDS);
        }

        FriendRequestEntity pending = friendRequestMapper.selectPendingBetween(requesterId, addresseeId);
        if (pending != null) {
            if (pending.getRequesterId().equals(requesterId)) {
                throw new BusinessException(ErrorCode.FRIEND_REQUEST_EXISTS);
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST, "对方已向你发送好友申请，请前往消息中心处理");
        }

        FriendRequestEntity existing = friendRequestMapper.selectByPair(requesterId, addresseeId);
        FriendRequestEntity saved;
        if (existing != null) {
            if (existing.getStatus() == FriendRequestStatus.PENDING) {
                throw new BusinessException(ErrorCode.FRIEND_REQUEST_EXISTS);
            }
            if (existing.getStatus() == FriendRequestStatus.ACCEPTED) {
                throw new BusinessException(ErrorCode.ALREADY_FRIENDS);
            }
            int resent = friendRequestMapper.resend(
                    existing.getId(), TextUtils.trimToMax(request.getMessage(), FRIEND_REQUEST_MESSAGE_MAX));
            if (resent == 0) {
                throw new BusinessException(ErrorCode.FRIEND_REQUEST_EXISTS);
            }
            saved = friendRequestMapper.selectById(existing.getId());
        } else {
            FriendRequestEntity entity = new FriendRequestEntity();
            entity.setRequesterId(requesterId);
            entity.setAddresseeId(addresseeId);
            entity.setMessage(TextUtils.trimToMax(request.getMessage(), FRIEND_REQUEST_MESSAGE_MAX));
            entity.setStatus(FriendRequestStatus.PENDING);
            friendRequestMapper.insert(entity);
            saved = entity;
        }

        friendNotificationService.notifyRequestSent(requester, saved);

        FriendRequestVO vo = friendRequestMapper.selectVoById(saved.getId());
        if (vo == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return vo;
    }

    @Transactional
    public void acceptRequest(Long requestId, Long userId) {
        FriendRequestEntity request = requirePendingRequest(requestId);
        if (!request.getAddresseeId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_FORBIDDEN);
        }
        if (friendshipMapper.exists(request.getRequesterId(), request.getAddresseeId())) {
            friendRequestMapper.updateStatus(requestId, FriendRequestStatus.ACCEPTED, FriendRequestStatus.PENDING);
            return;
        }

        int updated = friendRequestMapper.updateStatus(requestId, FriendRequestStatus.ACCEPTED, FriendRequestStatus.PENDING);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        friendshipMapper.insert(FriendshipPairs.of(request.getRequesterId(), request.getAddresseeId()));

        UserEntity addressee = userService.requireActiveUser(userId);
        friendNotificationService.notifyRequestAccepted(addressee, request.getRequesterId(), requestId);
    }

    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        FriendRequestEntity request = requirePendingRequest(requestId);
        if (!request.getAddresseeId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_FORBIDDEN);
        }
        int updated = friendRequestMapper.updateStatus(requestId, FriendRequestStatus.REJECTED, FriendRequestStatus.PENDING);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelRequest(Long requestId, Long userId) {
        FriendRequestEntity request = requirePendingRequest(requestId);
        if (!request.getRequesterId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_FORBIDDEN);
        }
        int updated = friendRequestMapper.updateStatus(requestId, FriendRequestStatus.CANCELLED, FriendRequestStatus.PENDING);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException(ErrorCode.FRIEND_SELF);
        }
        if (officialUserService.isOfficialUser(friendId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无法移除官方系统好友");
        }
        if (!friendshipMapper.exists(userId, friendId)) {
            throw new BusinessException(ErrorCode.NOT_FRIENDS);
        }
        friendshipMapper.deletePair(userId, friendId);
    }

    public String resolveFriendStatus(Long viewerId, Long targetUserId) {
        if (viewerId == null || targetUserId == null) {
            return FriendStatus.NONE;
        }
        if (viewerId.equals(targetUserId)) {
            return FriendStatus.NONE;
        }
        if (officialUserService.isOfficialUser(targetUserId)) {
            return FriendStatus.FRIENDS;
        }
        if (friendshipMapper.exists(viewerId, targetUserId)) {
            return FriendStatus.FRIENDS;
        }
        FriendRequestEntity pending = friendRequestMapper.selectPendingBetween(viewerId, targetUserId);
        if (pending == null) {
            return FriendStatus.NONE;
        }
        return pending.getRequesterId().equals(viewerId)
                ? FriendStatus.PENDING_SENT
                : FriendStatus.PENDING_RECEIVED;
    }

    public void requireFriendship(Long userId, Long friendId) {
        if (!areFriends(userId, friendId)) {
            throw new BusinessException(ErrorCode.NOT_FRIENDS);
        }
    }

    public boolean areFriends(Long userId, Long friendId) {
        return friendshipMapper.exists(userId, friendId);
    }

    private FriendRequestEntity requirePendingRequest(Long requestId) {
        FriendRequestEntity request = friendRequestMapper.selectById(requestId);
        if (request == null || request.getStatus() == null || request.getStatus() != FriendRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
        return request;
    }
}
