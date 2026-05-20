package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.HeartbeatRequestDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Presence", description = "Online/offline status for users: heartbeat and real-time status check")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceUseCase userPresenceUseCase;

    @Operation(summary = "Presence heartbeat", description = "The client calls this endpoint periodically to indicate the user is active. If the last heartbeat was more than 5 minutes ago, the user is set to OFFLINE. Also registers or updates the user's name and avatar.")
    @PutMapping("/{userId}/heartbeat")
    public ResponseEntity<UserPresenceResponseDTO> heartbeat(
            @PathVariable UUID userId,
            @Valid @RequestBody HeartbeatRequestDTO request) {
        return ResponseEntity.ok(userPresenceUseCase.heartbeat(userId, request.getEmail(), request.getName(), request.getAvatarUrl()));
    }

    @Operation(summary = "Get presence status", description = "Returns the current status of a user: ONLINE if they sent a heartbeat in the last 5 minutes, OFFLINE otherwise.")
    @GetMapping("/{userId}/status")
    public ResponseEntity<UserPresenceResponseDTO> getStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(userPresenceUseCase.getStatus(userId));
    }
}
