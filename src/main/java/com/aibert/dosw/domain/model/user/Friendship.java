package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Friendship {
    private UUID id;
    private UUID userId1;
    private UUID userId2;
    private LocalDateTime createdAt;
}
