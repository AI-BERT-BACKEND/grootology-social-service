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

@Tag(name = "Invitaciones", description = "Enlace de referido para externos y canje de códigos con puntos (RN-02/RN-03)")
@RestController
@RequestMapping("/api/social/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationUseCase invitationUseCase;

    @Operation(summary = "Obtener enlace de referido", description = "Genera o recupera el enlace único de referido del usuario para invitar personas externas a AI.BERT.")
    @GetMapping("/{userId}/link")
    public ResponseEntity<InviteResponseDTO> getReferralLink(@PathVariable UUID userId) {
        return ResponseEntity.ok(invitationUseCase.getOrCreateReferralLink(userId));
    }

    @Operation(summary = "Enviar invitaciones por correo", description = "Envía el enlace de referido por correo a usuarios externos. Valida que los correos no pertenezcan a usuarios ya registrados.")
    @PostMapping("/{userId}/send")
    public ResponseEntity<Map<String, String>> sendInvitations(
            @PathVariable UUID userId,
            @Valid @RequestBody InviteFriendsRequestDTO request) {
        invitationUseCase.sendInvitations(userId, request);
        return ResponseEntity.ok(Map.of("message", "Invitaciones enviadas exitosamente."));
    }

    @Operation(summary = "Canjear código de referido", description = "Registra el uso de un código de referido cuando un nuevo usuario completa el registro. Otorga puntos al invitador según RN-02 y respeta el límite semanal de RN-03.")
    @PostMapping("/{code}/redeem")
    public ResponseEntity<ReferralPointsResponseDTO> redeemCode(
            @PathVariable String code,
            @Valid @RequestBody RedeemReferralCodeRequestDTO request) {
        return ResponseEntity.ok(invitationUseCase.redeemReferralCode(code, request.getNewUserId()));
    }
}
