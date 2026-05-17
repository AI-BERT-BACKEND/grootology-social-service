package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;

import java.util.UUID;

public interface ReferralPointsUseCase {
    ReferralPointsResponseDTO awardPoints(UUID inviterId);
    ReferralPointsResponseDTO getPoints(UUID userId);
}
