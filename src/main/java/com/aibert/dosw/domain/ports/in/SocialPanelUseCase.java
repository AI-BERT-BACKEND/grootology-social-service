package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;

import java.util.UUID;

public interface SocialPanelUseCase {
    SocialPanelResponseDTO getPanel(UUID userId, SocialAction action);
}
