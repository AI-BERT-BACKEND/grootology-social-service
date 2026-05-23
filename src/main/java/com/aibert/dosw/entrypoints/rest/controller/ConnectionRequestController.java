package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
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
@Tag(name = "Connection Requests", description = "Manage friend requests: send, accept and reject between registered users in AI.BERT. (R02)")
@RestController
@RequestMapping("/api/social/connections")
@RequiredArgsConstructor
public class ConnectionRequestController {

    private final ConnectionRequestUseCase connectionRequestUseCase;

    @Operation(
        summary = "Send friend request",
        description = "Sends a friend request to another registered user. Requires a complete academic profile. Duplicate requests and self-requests are not allowed.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Friend request sent successfully")
    @ApiResponse(responseCode = "400", description = "Duplicate request, self-request or incomplete profile")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping("/{senderId}/request")
    public ResponseEntity<ConnectionRequestResponseDTO> sendRequest(
            @Parameter(description = "UUID of the sender", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID senderId,
            @Valid @RequestBody SendConnectionRequestDTO request) {
        log.info("POST /connections/{}/request - receiverId={}", senderId, request.getReceiverId());
        ConnectionRequestResponseDTO response = connectionRequestUseCase.sendRequest(senderId, request);
        log.debug("Connection request sent: requestId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Accept friend request",
        description = "Accepts a pending connection request. Only the receiver can do this. Once accepted, the friendship is automatically created.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Friend request accepted and friendship created")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only the receiver can accept this request")
    @ApiResponse(responseCode = "404", description = "Connection request not found")
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ConnectionRequestResponseDTO> acceptRequest(
            @Parameter(description = "UUID of the connection request", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        log.info("PUT /connections/{}/accept - receiverId={}", requestId, receiverId);
        return ResponseEntity.ok(connectionRequestUseCase.acceptRequest(requestId, receiverId));
    }

    @Operation(
        summary = "Reject friend request",
        description = "Rejects a pending connection request. Only the receiver can perform this action.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Friend request rejected")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only the receiver can reject this request")
    @ApiResponse(responseCode = "404", description = "Connection request not found")
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ConnectionRequestResponseDTO> rejectRequest(
            @Parameter(description = "UUID of the connection request", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID requestId,
            @RequestParam UUID receiverId) {
        log.info("PUT /connections/{}/reject - receiverId={}", requestId, receiverId);
        return ResponseEntity.ok(connectionRequestUseCase.rejectRequest(requestId, receiverId));
    }

    @Operation(
        summary = "Get received pending requests",
        description = "Returns all pending connection requests that the user has not yet responded to.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/pending/{receiverId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getPendingRequests(
            @Parameter(description = "UUID of the receiver", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID receiverId) {
        log.info("GET /connections/pending/{}", receiverId);
        return ResponseEntity.ok(connectionRequestUseCase.getPendingRequestsForUser(receiverId));
    }

    @Operation(
        summary = "Get sent requests",
        description = "Returns all connection requests sent by the user along with their current status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Sent requests retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ConnectionRequestResponseDTO>> getSentRequests(
            @Parameter(description = "UUID of the sender", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID senderId) {
        log.info("GET /connections/sent/{}", senderId);
        return ResponseEntity.ok(connectionRequestUseCase.getSentRequestsByUser(senderId));
    }
}
