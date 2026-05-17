package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.RelationshipStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.UserSearchUseCase;
import com.aibert.dosw.domain.ports.out.ConnectionRequestRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSearchService implements UserSearchUseCase {

    private final UserPresenceRepositoryPort userPresenceRepository;
    private final FriendshipRepositoryPort friendshipRepository;
    private final ConnectionRequestRepositoryPort connectionRequestRepository;

    @Override
    public List<UserSearchResultDTO> search(String query, UUID requesterId) {
        if (query == null || query.isBlank()) return List.of();

        return userPresenceRepository.searchByNameOrEmail(query.trim(), requesterId)
                .stream()
                .map(u -> buildResult(u, requesterId))
                .collect(Collectors.toList());
    }

    private UserSearchResultDTO buildResult(UserPresence u, UUID requesterId) {
        return UserSearchResultDTO.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .presenceStatus(UserPresenceResponseDTO.builder()
                        .userId(u.getUserId())
                        .status(u.getStatus())
                        .lastSeen(u.getLastSeen())
                        .avatarUrl(u.getAvatarUrl())
                        .build())
                .relationshipStatus(determineRelationship(requesterId, u.getUserId()))
                .build();
    }

    private RelationshipStatus determineRelationship(UUID requesterId, UUID targetId) {
        if (friendshipRepository.existsByUserIds(requesterId, targetId)) {
            return RelationshipStatus.FRIEND;
        }
        if (connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)) {
            return RelationshipStatus.PENDING_SENT;
        }
        if (connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                targetId, requesterId, ConnectionRequestStatus.PENDING)) {
            return RelationshipStatus.PENDING_RECEIVED;
        }
        return RelationshipStatus.NONE;
    }
}
