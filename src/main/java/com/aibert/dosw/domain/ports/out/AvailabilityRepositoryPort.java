package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityRepositoryPort {
    AvailabilityConfig save(AvailabilityConfig config);
    Optional<AvailabilityConfig> findByUserId(UUID userId);
}
