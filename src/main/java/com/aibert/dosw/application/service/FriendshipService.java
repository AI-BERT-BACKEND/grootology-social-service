package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService implements FriendshipUseCase {

    private final FriendshipRepositoryPort friendshipRepository;
    private final UserPresenceRepositoryPort userPresenceRepository;

    @Override
    public List<FriendshipResponseDTO> listFriends(UUID userId, OnlineStatus statusFilter) {
        log.info("Listing friends for userId={} statusFilter={}", userId, statusFilter);
        List<FriendshipResponseDTO> result = friendshipRepository.findByUserId(userId).stream()
                .map(f -> enrichFriendship(f, userId))
                .filter(dto -> statusFilter == null || dto.getPresenceStatus().getStatus() == statusFilter)
                .collect(Collectors.toList());
        log.debug("Found {} friends for userId={}", result.size(), userId);
        return result;
    }

    @Override
    public void removeFriend(UUID userId, UUID friendId) {
        log.info("Removing friendship between userId={} and friendId={}", userId, friendId);
        if (!friendshipRepository.existsByUserIds(userId, friendId)) {
            log.warn("Friendship not found between userId={} and friendId={}", userId, friendId);
            throw new ConnectionRequestException("No existe una amistad entre estos usuarios");
        }
        friendshipRepository.deleteByUserIds(userId, friendId);
        log.debug("Friendship removed between userId={} and friendId={}", userId, friendId);
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
                .friendAvatarUrl(presence != null ? presence.getAvatarUrl() : null)
                .presenceStatus(UserPresenceResponseDTO.builder()
                        .userId(friendId)
                        .status(status)
                        .lastSeen(presence != null ? presence.getLastSeen() : null)
                        .avatarUrl(presence != null ? presence.getAvatarUrl() : null)
                        .build())
                .since(f.getCreatedAt())
                .build();
    }
}
