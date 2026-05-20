package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.request.RedeemReferralCodeRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Invitations", description = "Referral links for external users and code redemption with points")
@RestController
@RequestMapping("/api/social/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationUseCase invitationUseCase;

    @Operation(summary = "Get referral link", description = "Generates or retrieves the user's unique referral link to invite people outside AI.BERT.")
    @GetMapping("/{userId}/link")
    public ResponseEntity<InviteResponseDTO> getReferralLink(@PathVariable UUID userId) {
        return ResponseEntity.ok(invitationUseCase.getOrCreateReferralLink(userId));
    }

    @Operation(summary = "Send invitations by email", description = "Sends the referral link by email to external users. Validates that the provided emails do not belong to already registered users.")
    @PostMapping("/{userId}/send")
    public ResponseEntity<Map<String, String>> sendInvitations(
            @PathVariable UUID userId,
            @Valid @RequestBody InviteFriendsRequestDTO request) {
        invitationUseCase.sendInvitations(userId, request);
        return ResponseEntity.ok(Map.of("message", "Invitations sent successfully."));
    }

    @Operation(summary = "Redeem referral code", description = "Registers the use of a referral code when a new user completes registration. Awards points to the referrer and respects the weekly limit.")
    @PostMapping("/{code}/redeem")
    public ResponseEntity<ReferralPointsResponseDTO> redeemCode(
            @PathVariable String code,
            @Valid @RequestBody RedeemReferralCodeRequestDTO request) {
        return ResponseEntity.ok(invitationUseCase.redeemReferralCode(code, request.getNewUserId()));
    }
}
