package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Study Session Invites", description = "Retrieve pending study session invitations for a participant user. (R04)")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class StudyInviteController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(
            summary = "Get pending study invites",
            description = "Returns study sessions with status PENDING where the user is a participant (not the creator). Full path: GET /api/social/users/{userId}/study-invites/pending.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Pending study invites retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/{userId}/study-invites/pending")
    public ResponseEntity<List<StudySessionResponseDTO>> getPendingInvites(
            @Parameter(description = "UUID of the participant user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getPendingInvitesForUser(userId));
    }
}
