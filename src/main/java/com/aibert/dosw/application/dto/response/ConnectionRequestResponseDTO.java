package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ConnectionRequestResponseDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private ConnectionRequestStatus status;
    private LocalDateTime sentAt;
}
