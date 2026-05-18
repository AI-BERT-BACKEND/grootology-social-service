package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;

import java.util.List;
import java.util.UUID;

public interface FriendshipUseCase {
    List<FriendshipResponseDTO> listFriends(UUID userId, OnlineStatus statusFilter);
    void removeFriend(UUID userId, UUID friendId);
}
