package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.SocialAction;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SocialPanelResponseDTO {
    private UUID userId;
    private SocialAction selectedAction;
    private List<SocialActionItemResponseDTO> availableActions;
    private UserPresenceResponseDTO presenceStatus;
    private InviteResponseDTO invite;
    private List<ConnectionRequestResponseDTO> pendingConnectionRequests;
    private AvailabilityConfig availability;
    private List<StudySessionResponseDTO> sessions;
    private String chatStatus;
}
