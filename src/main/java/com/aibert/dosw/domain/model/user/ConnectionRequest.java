package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ConnectionRequest {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private ConnectionRequestStatus status;
    private LocalDateTime sentAt;
}
