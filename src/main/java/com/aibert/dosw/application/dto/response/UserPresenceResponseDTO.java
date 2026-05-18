package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.OnlineStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserPresenceResponseDTO {
    private UUID userId;
    private OnlineStatus status;
    private LocalDateTime lastSeen;
    private String avatarUrl;
}
