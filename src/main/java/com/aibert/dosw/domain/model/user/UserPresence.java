package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserPresence {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String avatarUrl;
    private LocalDateTime lastSeen;

    public OnlineStatus getStatus() {
        if (lastSeen == null) return OnlineStatus.OFFLINE;
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(5))
                ? OnlineStatus.ONLINE : OnlineStatus.OFFLINE;
    }
}
