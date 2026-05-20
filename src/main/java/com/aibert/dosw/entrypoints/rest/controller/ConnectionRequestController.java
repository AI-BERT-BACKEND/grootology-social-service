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

@Tag(name = "Connection Requests", description = "Send, accept and reject friend requests between registered users in AI.BERT")
@RestController
@RequestMapping("/api/social/connections")
@RequiredArgsConstructor
public class ConnectionRequestController {

    private final ConnectionRequestUseCase connectionRequestUseCase;

    @Operation(summary = "Send friend request", description = "Sends a friend request to another registered user. Requires a complete academic profile. Duplicate requests and self-requests are not allowed.")
    @PostMapping("/{senderId}/request")
    public ResponseEntity<ConnectionRequestResponseDTO> sendRequest(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendConnectionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionRequestUseCase.sendRequest(senderId, request));
    }

    @Operation(summary = "Accept friend request", description = "Accepts a pending connection request. Only the receiver can do this. Once accepted, the friendship is automatically created.")
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ConnectionRequestResponseDTO> acceptRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.acceptRequest(requestId, receiverId));
    }

    @Operation(summary = "Reject friend request", description = "Rejects a pending connection request. Only the receiver can perform this action.")
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ConnectionRequestResponseDTO> rejectRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.rejectRequest(requestId, receiverId));
    }

    @Operation(summary = "Get received pending requests", description = "Returns all pending connection requests that the user has not yet responded to.")
    @GetMapping("/pending/{receiverId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getPendingRequests(
            @PathVariable UUID receiverId) {
        return ResponseEntity.ok(connectionRequestUseCase.getPendingRequestsForUser(receiverId));
    }

    @Operation(summary = "Get sent requests", description = "Returns all connection requests sent by the user along with their current status.")
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getSentRequests(
            @PathVariable UUID senderId) {
        return ResponseEntity.ok(connectionRequestUseCase.getSentRequestsByUser(senderId));
    }
}
