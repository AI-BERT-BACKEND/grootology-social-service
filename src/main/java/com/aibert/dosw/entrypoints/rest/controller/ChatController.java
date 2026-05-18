package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.domain.ports.in.ChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @PostMapping("/{senderId}/messages")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendMessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatUseCase.sendMessage(senderId, request));
    }

    @GetMapping("/conversations/{userId}/{friendId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> getConversation(
            @PathVariable UUID userId,
            @PathVariable UUID friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatUseCase.getConversation(userId, friendId, page, size));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryResponseDTO>> getConversations(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(chatUseCase.getConversations(userId));
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatMessageResponseDTO> markAsRead(
            @PathVariable UUID messageId,
            @RequestParam UUID readerId) {
        return ResponseEntity.ok(chatUseCase.markAsRead(messageId, readerId));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {
        chatUseCase.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
