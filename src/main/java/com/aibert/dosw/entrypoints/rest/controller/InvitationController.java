package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.request.RedeemReferralCodeRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Invitations", description = "Manage referral links for external users and code redemption with points. (R01)")
@RestController
@RequestMapping("/api/social/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationUseCase invitationUseCase;

    @Operation(
        summary = "Get referral link",
        description = "Generates or retrieves the user's unique referral link to invite people outside AI.BERT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Referral link retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/{userId}/link")
    public ResponseEntity<InviteResponseDTO> getReferralLink(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId) {
        return ResponseEntity.ok(invitationUseCase.getOrCreateReferralLink(userId));
    }

    @Operation(
        summary = "Send invitations by email",
        description = "Sends the referral link by email to external users. Validates that the provided emails do not belong to already registered users.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Invitations sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid email format or email already registered")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping("/{userId}/send")
    public ResponseEntity<Map<String, String>> sendInvitations(
            @Parameter(description = "UUID of the user sending the invitations", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Valid @RequestBody InviteFriendsRequestDTO request) {
        invitationUseCase.sendInvitations(userId, request);
        return ResponseEntity.ok(Map.of("message", "Invitations sent successfully."));
    }

    @Operation(
        summary = "Redeem referral code",
        description = "Registers the use of a referral code when a new user completes registration. Awards points to the referrer and respects the weekly limit.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Referral code redeemed and points awarded")
    @ApiResponse(responseCode = "400", description = "Invalid or already used referral code")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Referral code not found")
    @PostMapping("/{code}/redeem")
    public ResponseEntity<ReferralPointsResponseDTO> redeemCode(
            @Parameter(description = "Referral code string", example = "INV-A1B2C3", schema = @Schema(type = "string"))
            @PathVariable String code,
            @Valid @RequestBody RedeemReferralCodeRequestDTO request) {
        return ResponseEntity.ok(invitationUseCase.redeemReferralCode(code, request.getNewUserId()));
    }
}
