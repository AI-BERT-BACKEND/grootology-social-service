package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/connections")
@RequiredArgsConstructor
public class ConnectionRequestController {

    private final ConnectionRequestUseCase connectionRequestUseCase;

    @PostMapping("/{senderId}/request")
    public ResponseEntity<ConnectionRequestResponseDTO> sendRequest(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendConnectionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionRequestUseCase.sendRequest(senderId, request));
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ConnectionRequestResponseDTO> acceptRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.acceptRequest(requestId, receiverId));
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ConnectionRequestResponseDTO> rejectRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.rejectRequest(requestId, receiverId));
    }

    @GetMapping("/pending/{receiverId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getPendingRequests(
            @PathVariable UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.getPendingRequestsForUser(receiverId));
    }

    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getSentRequests(
            @PathVariable UUID senderId) {
        return ResponseEntity.ok(connectionRequestUseCase.getSentRequestsByUser(senderId));
    }
}
