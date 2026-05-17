package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;

import java.util.UUID;

public interface UserPresenceUseCase {
    UserPresenceResponseDTO heartbeat(UUID userId, String email);
    UserPresenceResponseDTO getStatus(UUID userId);
}
