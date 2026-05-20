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

@Tag(name = "Presencia", description = "Estado online/offline de usuarios: heartbeat y consulta de estado en tiempo real")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceUseCase userPresenceUseCase;

    @Operation(summary = "Heartbeat de presencia", description = "El cliente llama a este endpoint periódicamente para indicar que el usuario está activo. Si el último heartbeat supera 5 minutos, el usuario pasa a OFFLINE. También registra o actualiza nombre y avatar.")
    @PutMapping("/{userId}/heartbeat")
    public ResponseEntity<UserPresenceResponseDTO> heartbeat(
            @PathVariable UUID userId,
            @Valid @RequestBody HeartbeatRequestDTO request) {
        return ResponseEntity.ok(userPresenceUseCase.heartbeat(userId, request.getEmail(), request.getName(), request.getAvatarUrl()));
    }

    @Operation(summary = "Consultar estado de presencia", description = "Devuelve el estado actual de un usuario: ONLINE si hizo heartbeat en los últimos 5 minutos, OFFLINE en caso contrario.")
    @GetMapping("/{userId}/status")
    public ResponseEntity<UserPresenceResponseDTO> getStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(userPresenceUseCase.getStatus(userId));
    }
}
