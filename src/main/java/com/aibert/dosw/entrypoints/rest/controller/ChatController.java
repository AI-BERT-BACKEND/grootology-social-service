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

@Tag(name = "Chat", description = "Mensajería en tiempo real entre amigos: enviar, leer y eliminar mensajes")
@RestController
@RequestMapping("/api/social/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @Operation(summary = "Enviar mensaje", description = "Envía un mensaje a un amigo. Solo se permite entre usuarios que ya son amigos.")
    @PostMapping("/{senderId}/messages")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @PathVariable UUID senderId,
            @Valid @RequestBody SendMessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatUseCase.sendMessage(senderId, request));
    }

    @Operation(summary = "Obtener conversación", description = "Devuelve el historial de mensajes entre dos usuarios, paginado. El más reciente primero.")
    @GetMapping("/conversations/{userId}/{friendId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> getConversation(
            @PathVariable UUID userId,
            @PathVariable UUID friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatUseCase.getConversation(userId, friendId, page, size));
    }

    @Operation(summary = "Listar conversaciones", description = "Devuelve el resumen de todas las conversaciones activas del usuario: último mensaje, estado online del amigo y mensajes no leídos.")
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryResponseDTO>> getConversations(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(chatUseCase.getConversations(userId));
    }

    @Operation(summary = "Marcar como leído", description = "Marca un mensaje como leído. Solo puede hacerlo el receptor del mensaje.")
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatMessageResponseDTO> markAsRead(
            @PathVariable UUID messageId,
            @RequestParam UUID readerId) {
        return ResponseEntity.ok(chatUseCase.markAsRead(messageId, readerId));
    }

    @Operation(summary = "Eliminar mensaje", description = "Elimina un mensaje para el usuario indicado (eliminación lógica). Si ambos lo eliminan, el mensaje desaparece de la conversación.")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {
        chatUseCase.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
