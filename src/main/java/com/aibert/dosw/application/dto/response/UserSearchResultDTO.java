package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.RelationshipStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSearchResultDTO {
    private UUID userId;
    private String name;
    private String email;
    private String avatarUrl;
    private UserPresenceResponseDTO presenceStatus;
    private RelationshipStatus relationshipStatus;
}
