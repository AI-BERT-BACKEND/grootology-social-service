package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.ReferralPoints;

import java.util.Optional;
import java.util.UUID;

public interface ReferralPointsRepositoryPort {
    ReferralPoints save(ReferralPoints points);
    Optional<ReferralPoints> findByUserId(UUID userId);
}
