package com.aibert.dosw.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ConversationSummaryResponseDTO {
    private UUID friendId;
    private String friendName;
    private String friendAvatarUrl;
    private UserPresenceResponseDTO presenceStatus;
    private String lastMessageContent;
    private UUID lastMessageSenderId;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
