package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.domain.ports.in.ChatUseCase;
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
@Tag(name = "Chat", description = "Manage real-time messaging between friends: send, read and delete messages. (R05)")
@RestController
@RequestMapping("/api/social/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @Operation(
        summary = "Send a message",
        description = "Sends a message to a friend. Only allowed between users who are already friends.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Message sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body or users are not friends")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @PostMapping("/{senderId}/messages")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @Parameter(description = "UUID of the sender", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID senderId,
            @Valid @RequestBody SendMessageRequestDTO request) {
        log.info("POST /chat/{}/messages - receiverId={}", senderId, request.getReceiverId());
        ChatMessageResponseDTO response = chatUseCase.sendMessage(senderId, request);
        log.debug("Message sent: messageId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get conversation",
        description = "Returns the message history between two users, paginated with the most recent messages first.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Conversation history retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "User or friend not found")
    @GetMapping("/conversations/{userId}/{friendId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> getConversation(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Parameter(description = "UUID of the friend", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /chat/conversations/{}/{} - page={} size={}", userId, friendId, page, size);
        return ResponseEntity.ok(chatUseCase.getConversation(userId, friendId, page, size));
    }

    @Operation(
        summary = "List conversations",
        description = "Returns a summary of all active conversations for the user, including the last message, the friend's online status and unread message count.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Conversation list retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryResponseDTO>> getConversations(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId) {
        log.info("GET /chat/conversations/{}", userId);
        return ResponseEntity.ok(chatUseCase.getConversations(userId));
    }

    @Operation(
        summary = "Mark message as read",
        description = "Marks a message as read. Only the receiver of the message can perform this action.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Message marked as read")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Only the receiver can mark this message as read")
    @ApiResponse(responseCode = "404", description = "Message not found")
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatMessageResponseDTO> markAsRead(
            @Parameter(description = "UUID of the message", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID messageId,
            @RequestParam UUID readerId) {
        log.info("PUT /chat/messages/{}/read - readerId={}", messageId, readerId);
        return ResponseEntity.ok(chatUseCase.markAsRead(messageId, readerId));
    }

    @Operation(
        summary = "Delete message",
        description = "Deletes a message for the given user (soft delete). If both users delete it, the message disappears from the conversation entirely.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Message deleted successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Message not found")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "UUID of the message", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {
        log.info("DELETE /chat/messages/{} - userId={}", messageId, userId);
        chatUseCase.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
