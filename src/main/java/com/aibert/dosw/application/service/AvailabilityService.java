package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import com.aibert.dosw.domain.ports.out.AvailabilityRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService implements AvailabilityUseCase {

    private final AvailabilityRepositoryPort repository;

    @Override
    public AvailabilityConfig saveConfig(UUID userId, AvailabilityConfigRequestDTO request) {
        return repository.save(AvailabilityConfig.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .visibility(request.getVisibility())
                .authorizedFriends(request.getAuthorizedFriends())
                .build());
    }

    @Override
    public AvailabilityConfig getConfig(UUID userId) {
        return repository.findByUserId(userId)
                .orElse(AvailabilityConfig.builder()
                        .userId(userId)
                        .visibility(com.aibert.dosw.domain.model.user.VisibilityLevel.PRIVATE)
                        .build());
    }
}
