package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import java.util.UUID;

public interface InvitationUseCase {
    InviteResponseDTO getOrCreateReferralLink(UUID userId);
    void sendInvitations(UUID userId, InviteFriendsRequestDTO request);
}
