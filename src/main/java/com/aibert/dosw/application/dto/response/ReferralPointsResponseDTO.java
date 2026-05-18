package com.aibert.dosw.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReferralPointsResponseDTO {
    private UUID userId;
    private int totalPoints;
    private int weeklyPoints;
    private int weeklyLimit;
    private int pointsAwarded;
    private boolean weeklyLimitReached;
}
