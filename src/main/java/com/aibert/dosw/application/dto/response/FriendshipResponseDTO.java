package com.aibert.dosw.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class FriendshipResponseDTO {
    private UUID id;
    private UUID friendId;
    private String friendName;
    private String friendEmail;
    private UserPresenceResponseDTO presenceStatus;
    private LocalDateTime since;
}
