package com.aibert.dosw.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChatMessageResponseDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private boolean read;
}
