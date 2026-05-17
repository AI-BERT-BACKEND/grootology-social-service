package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPresenceService implements UserPresenceUseCase {

    private final UserPresenceRepositoryPort presenceRepository;

    @Override
    public UserPresenceResponseDTO heartbeat(UUID userId, String email) {
        UserPresence updated = presenceRepository.findByUserId(userId)
                .map(existing -> UserPresence.builder()
                        .id(existing.getId())
                        .userId(userId)
                        .email(email != null ? email : existing.getEmail())
                        .lastSeen(LocalDateTime.now())
                        .build())
                .orElse(UserPresence.builder()
                        .userId(userId)
                        .email(email)
                        .lastSeen(LocalDateTime.now())
                        .build());

        return toDTO(presenceRepository.save(updated));
    }

    @Override
    public UserPresenceResponseDTO getStatus(UUID userId) {
        return presenceRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElse(UserPresenceResponseDTO.builder()
                        .userId(userId)
                        .status(OnlineStatus.OFFLINE)
                        .lastSeen(null)
                        .build());
    }

    private UserPresenceResponseDTO toDTO(UserPresence p) {
        return UserPresenceResponseDTO.builder()
                .userId(p.getUserId())
                .status(p.getStatus())
                .lastSeen(p.getLastSeen())
                .build();
    }
}
