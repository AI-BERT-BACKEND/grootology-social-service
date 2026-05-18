package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.request.RedeemReferralCodeRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationUseCase invitationUseCase;

    @GetMapping("/{userId}/link")
    public ResponseEntity<InviteResponseDTO> getReferralLink(@PathVariable UUID userId) {
        return ResponseEntity.ok(invitationUseCase.getOrCreateReferralLink(userId));
    }

    @PostMapping("/{userId}/send")
    public ResponseEntity<Map<String, String>> sendInvitations(
            @PathVariable UUID userId,
            @Valid @RequestBody InviteFriendsRequestDTO request) {
        invitationUseCase.sendInvitations(userId, request);
        return ResponseEntity.ok(Map.of("message", "Invitaciones enviadas exitosamente."));
    }

    @PostMapping("/{code}/redeem")
    public ResponseEntity<ReferralPointsResponseDTO> redeemCode(
            @PathVariable String code,
            @Valid @RequestBody RedeemReferralCodeRequestDTO request) {
        return ResponseEntity.ok(invitationUseCase.redeemReferralCode(code, request.getNewUserId()));
    }
}
