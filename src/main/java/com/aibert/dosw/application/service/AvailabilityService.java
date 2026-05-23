package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import com.aibert.dosw.domain.ports.out.AvailabilityRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService implements AvailabilityUseCase {

    private final AvailabilityRepositoryPort repository;

    @Override
    public AvailabilityConfig saveConfig(UUID userId, AvailabilityConfigRequestDTO request) {
        log.info("Saving availability config for userId={} visibility={}", userId, request.getVisibility());
        // Reutiliza el id si el usuario ya tiene configuracion (UPDATE);
        // si no existe, deja el id en null para que JPA haga INSERT (@GeneratedValue).
        UUID existingId = repository.findByUserId(userId)
                .map(AvailabilityConfig::getId)
                .orElse(null);

        AvailabilityConfig saved = repository.save(AvailabilityConfig.builder()
                .id(existingId)
                .userId(userId)
                .visibility(request.getVisibility())
                .authorizedFriends(request.getAuthorizedFriends())
                .build());
        log.debug("Availability config saved for userId={}", userId);
        return saved;
    }

    @Override
    public AvailabilityConfig getConfig(UUID userId) {
        log.debug("Getting availability config for userId={}", userId);
        return repository.findByUserId(userId)
                .orElse(AvailabilityConfig.builder()
                        .userId(userId)
                        .visibility(VisibilityLevel.PRIVATE)
                        .build());
    }
}
