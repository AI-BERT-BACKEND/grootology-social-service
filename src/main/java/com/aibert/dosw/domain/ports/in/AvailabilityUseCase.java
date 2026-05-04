package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import java.util.UUID;

public interface AvailabilityUseCase {
    AvailabilityConfig saveConfig(UUID userId, AvailabilityConfigRequestDTO request);
    AvailabilityConfig getConfig(UUID userId);
}
