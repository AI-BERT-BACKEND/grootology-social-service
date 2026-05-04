package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AvailabilityConfig {
    private UUID id;
    private UUID userId;
    private VisibilityLevel visibility;
    private List<UUID> authorizedFriends;
}
