package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Solicitudes de conexión", description = "Enviar, aceptar y rechazar solicitudes de amistad entre usuarios registrados en AI.BERT")
@RestController
@RequestMapping("/api/social/connections")
@RequiredArgsConstructor
public class ConnectionRequestController {

    private final ConnectionRequestUseCase connectionRequestUseCase;

    @Operation(summary = "Enviar solicitud", description = "Envía una solicitud de amistad a otro usuario registrado. Requiere perfil académico completo (RN-02). No se permiten solicitudes duplicadas ni auto-solicitudes.")
    @PostMapping("/{senderId}/request")
    public ResponseEntity<ConnectionRequestResponseDTO> sendRequest(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendConnectionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionRequestUseCase.sendRequest(senderId, request));
    }

    @Operation(summary = "Aceptar solicitud", description = "Acepta una solicitud de conexión pendiente. Solo puede ejecutarlo el receptor. Al aceptar se crea automáticamente la amistad.")
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ConnectionRequestResponseDTO> acceptRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.acceptRequest(requestId, receiverId));
    }

    @Operation(summary = "Rechazar solicitud", description = "Rechaza una solicitud de conexión pendiente. Solo puede ejecutarlo el receptor.")
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ConnectionRequestResponseDTO> rejectRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.rejectRequest(requestId, receiverId));
    }

    @Operation(summary = "Solicitudes pendientes recibidas", description = "Devuelve todas las solicitudes de conexión que el usuario tiene pendientes de responder.")
    @GetMapping("/pending/{receiverId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getPendingRequests(
            @PathVariable UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.getPendingRequestsForUser(receiverId));
    }

    @Operation(summary = "Solicitudes enviadas", description = "Devuelve todas las solicitudes de conexión enviadas por el usuario, con su estado actual.")
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getSentRequests(
            @PathVariable UUID senderId) {
        return ResponseEntity.ok(connectionRequestUseCase.getSentRequestsByUser(senderId));
    }
}
