package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.HeartbeatRequestDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "User Presence", description = "Online/offline status for users: heartbeat and real-time status check")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceUseCase userPresenceUseCase;

    @Operation(
        summary = "Presence heartbeat",
        description = "The client calls this endpoint periodically to indicate the user is active. If the last heartbeat was more than 5 minutes ago, the user is set to OFFLINE. Also registers or updates the user's name and avatar.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Heartbeat registered and presence updated")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PutMapping("/{userId}/heartbeat")
    public ResponseEntity<UserPresenceResponseDTO> heartbeat(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Valid @RequestBody HeartbeatRequestDTO request) {
        log.debug("PUT /users/{}/heartbeat", userId);
        return ResponseEntity.ok(userPresenceUseCase.heartbeat(userId, request.getEmail(), request.getName(), request.getAvatarUrl()));
    }

    @Operation(
        summary = "Get presence status",
        description = "Returns the current status of a user: ONLINE if they sent a heartbeat in the last 5 minutes, OFFLINE otherwise.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Presence status retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}/status")
    public ResponseEntity<UserPresenceResponseDTO> getStatus(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId) {
        log.info("GET /users/{}/status", userId);
        return ResponseEntity.ok(userPresenceUseCase.getStatus(userId));
    }
}
