package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;

import java.util.UUID;

public interface InvitationUseCase {
    InviteResponseDTO getOrCreateReferralLink(UUID userId);
    void sendInvitations(UUID userId, InviteFriendsRequestDTO request);
    ReferralPointsResponseDTO redeemReferralCode(String code, UUID newUserId);
}
