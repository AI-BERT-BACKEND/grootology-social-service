package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService implements FriendshipUseCase {

    private final FriendshipRepositoryPort friendshipRepository;
    private final UserPresenceRepositoryPort userPresenceRepository;

    @Override
    public List<FriendshipResponseDTO> listFriends(UUID userId, OnlineStatus statusFilter) {
        return friendshipRepository.findByUserId(userId).stream()
                .map(f -> enrichFriendship(f, userId))
                .filter(dto -> statusFilter == null || dto.getPresenceStatus().getStatus() == statusFilter)
                .collect(Collectors.toList());
    }

    private FriendshipResponseDTO enrichFriendship(Friendship f, UUID userId) {
        UUID friendId = f.getUserId1().equals(userId) ? f.getUserId2() : f.getUserId1();
        UserPresence presence = userPresenceRepository.findByUserId(friendId).orElse(null);

        OnlineStatus status = presence != null ? presence.getStatus() : OnlineStatus.OFFLINE;

        return FriendshipResponseDTO.builder()
                .id(f.getId())
                .friendId(friendId)
                .friendName(presence != null ? presence.getName() : null)
                .friendEmail(presence != null ? presence.getEmail() : null)
                .presenceStatus(UserPresenceResponseDTO.builder()
                        .userId(friendId)
                        .status(status)
                        .lastSeen(presence != null ? presence.getLastSeen() : null)
                        .build())
                .since(f.getCreatedAt())
                .build();
    }
}
