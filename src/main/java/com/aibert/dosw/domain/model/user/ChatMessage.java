package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class ChatMessage {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private boolean deletedBySender;
    private boolean deletedByReceiver;
}
