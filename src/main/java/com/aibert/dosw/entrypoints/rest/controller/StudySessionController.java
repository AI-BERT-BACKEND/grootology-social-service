package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Study Sessions", description = "Create, respond to and list collaborative study sessions")
@RestController
@RequestMapping("/api/social/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(
        summary = "Create study session",
        description = "Creates a new study session with participants, topic, date and duration. The creator is automatically included as a participant.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Study session created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or participant list")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping("/{creatorId}")
    public ResponseEntity<StudySessionResponseDTO> createSession(
            @Parameter(description = "UUID of the session creator", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID creatorId,
            @Valid @RequestBody CreateStudySessionRequestDTO request) {
        log.info("POST /sessions/{} - topic={} participants={}", creatorId, request.getTopic(), request.getParticipantIds() != null ? request.getParticipantIds().size() : 0);
        StudySessionResponseDTO response = studySessionUseCase.createSession(creatorId, request);
        log.debug("Study session created: sessionId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Respond to session invite",
        description = "Accepts or rejects a study session invitation. Use `accept=true` to accept and `accept=false` to reject.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Response to session invite recorded")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "User is not a participant of this session")
    @ApiResponse(responseCode = "404", description = "Study session not found")
    @PutMapping("/{sessionId}/respond")
    public ResponseEntity<StudySessionResponseDTO> respond(
            @Parameter(description = "UUID of the study session", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID sessionId,
            @Parameter(description = "UUID of the responding user", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901", schema = @Schema(type = "string", format = "uuid"))
            @RequestParam UUID userId,
            @RequestParam boolean accept) {
        log.info("PUT /sessions/{}/respond - userId={} accept={}", sessionId, userId, accept);
        return ResponseEntity.ok(studySessionUseCase.respondToSession(sessionId, userId, accept));
    }

    @Operation(
        summary = "List user sessions",
        description = "Returns all study sessions where the user is either the creator or a participant.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Study sessions retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudySessionResponseDTO>> getSessionsForUser(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId) {
        log.info("GET /sessions/user/{}", userId);
        return ResponseEntity.ok(studySessionUseCase.getSessionsForUser(userId));
    }
}
