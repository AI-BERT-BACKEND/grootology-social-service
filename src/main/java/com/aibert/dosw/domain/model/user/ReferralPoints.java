package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class ReferralPoints {
    private UUID id;
    private UUID userId;
    private int totalPoints;
    private int weeklyPoints;
    private LocalDate weekStart;
}
