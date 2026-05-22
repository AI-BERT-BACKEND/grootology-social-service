package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.domain.ports.in.ChatUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat", description = "Real-time messaging between friends: send, read and delete messages")
@RestController
@RequestMapping("/api/social/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @Operation(summary = "Send a message", description = "Sends a message to a friend. Only allowed between users who are already friends.")
    @PostMapping("/{senderId}/messages")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendMessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatUseCase.sendMessage(senderId, request));
    }

    @Operation(summary = "Get conversation", description = "Returns the message history between two users, paginated with the most recent messages first.")
    @GetMapping("/conversations/{userId}/{friendId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> getConversation(
            @PathVariable UUID userId,
            @PathVariable UUID friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatUseCase.getConversation(userId, friendId, page, size));
    }

    @Operation(summary = "List conversations", description = "Returns a summary of all active conversations for the user, including the last message, the friend's online status and unread message count.")
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryResponseDTO>> getConversations(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(chatUseCase.getConversations(userId));
    }

    @Operation(summary = "Mark message as read", description = "Marks a message as read. Only the receiver of the message can perform this action.")
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatMessageResponseDTO> markAsRead(
            @PathVariable UUID messageId,
            @RequestParam UUID readerId) {
        return ResponseEntity.ok(chatUseCase.markAsRead(messageId, readerId));
    }

    @Operation(summary = "Delete message", description = "Deletes a message for the given user (soft delete). If both users delete it, the message disappears from the conversation entirely.")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {
        chatUseCase.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
