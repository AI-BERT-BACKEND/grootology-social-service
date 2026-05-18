package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.HeartbeatRequestDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceUseCase userPresenceUseCase;

    @PutMapping("/{userId}/heartbeat")
    public ResponseEntity<UserPresenceResponseDTO> heartbeat(
            @PathVariable UUID userId,
            @Valid @RequestBody HeartbeatRequestDTO request) {
        return ResponseEntity.ok(userPresenceUseCase.heartbeat(userId, request.getEmail(), request.getName(), request.getAvatarUrl()));
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<UserPresenceResponseDTO> getStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(userPresenceUseCase.getStatus(userId));
    }
}
