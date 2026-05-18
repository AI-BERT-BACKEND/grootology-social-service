package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.domain.ports.in.ChatUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatControllerTest {

    private final ChatUseCase useCase = mock(ChatUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    private ChatMessageResponseDTO buildMsg(UUID senderId, UUID receiverId) {
        return ChatMessageResponseDTO.builder()
                .id(UUID.randomUUID()).senderId(senderId).receiverId(receiverId)
                .content("Hola").sentAt(LocalDateTime.now()).read(false).build();
    }

    @Test
    void sendMessage_retorna201() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        when(useCase.sendMessage(eq(senderId), any())).thenReturn(buildMsg(senderId, receiverId));

        mockMvc.perform(post("/api/social/chat/{senderId}/messages", senderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                Map.of("receiverId", receiverId, "content", "Hola!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hola"));
    }

    @Test
    void getConversation_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        when(useCase.getConversation(userId, friendId, 0, 20))
                .thenReturn(List.of(buildMsg(userId, friendId)));

        mockMvc.perform(get("/api/social/chat/conversations/{userId}/{friendId}", userId, friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hola"));
    }

    @Test
    void getConversations_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        when(useCase.getConversations(userId)).thenReturn(List.of(
                ConversationSummaryResponseDTO.builder()
                        .friendId(friendId).friendName("Ana")
                        .lastMessageContent("Hola")
                        .lastMessageSenderId(friendId)
                        .lastMessageAt(LocalDateTime.now()).unreadCount(1).build()));

        mockMvc.perform(get("/api/social/chat/conversations/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unreadCount").value(1));
    }

    @Test
    void markAsRead_retorna200() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID readerId = UUID.randomUUID();
        ChatMessageResponseDTO dto = ChatMessageResponseDTO.builder()
                .id(messageId).senderId(UUID.randomUUID()).receiverId(readerId)
                .content("Hola").sentAt(LocalDateTime.now())
                .readAt(LocalDateTime.now()).read(true).build();

        when(useCase.markAsRead(messageId, readerId)).thenReturn(dto);

        mockMvc.perform(put("/api/social/chat/messages/{messageId}/read", messageId)
                        .param("readerId", readerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void deleteMessage_retorna204() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doNothing().when(useCase).deleteMessage(messageId, userId);

        mockMvc.perform(delete("/api/social/chat/messages/{messageId}", messageId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(useCase).deleteMessage(messageId, userId);
    }
}
