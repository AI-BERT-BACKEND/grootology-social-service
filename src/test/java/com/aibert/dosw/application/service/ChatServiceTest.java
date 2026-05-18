package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.ChatMessage;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.out.ChatMessageRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatMessageRepositoryPort messageRepository;
    @Mock private FriendshipRepositoryPort friendshipRepository;
    @Mock private UserPresenceRepositoryPort userPresenceRepository;
    @InjectMocks private ChatService chatService;

    private final UUID senderId = UUID.randomUUID();
    private final UUID receiverId = UUID.randomUUID();

    private ChatMessage buildMessage(UUID id, UUID from, UUID to, LocalDateTime readAt) {
        return ChatMessage.builder()
                .id(id).senderId(from).receiverId(to)
                .content("Hola").sentAt(LocalDateTime.now().minusMinutes(5))
                .readAt(readAt)
                .deletedBySender(false).deletedByReceiver(false).build();
    }

    @Test
    void sendMessage_exitoso_guardaMensaje() {
        SendMessageRequestDTO dto = mock(SendMessageRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(receiverId);
        when(dto.getContent()).thenReturn("Hola!");
        when(friendshipRepository.existsByUserIds(senderId, receiverId)).thenReturn(true);
        when(messageRepository.save(any())).thenReturn(buildMessage(UUID.randomUUID(), senderId, receiverId, null));

        ChatMessageResponseDTO result = chatService.sendMessage(senderId, dto);

        assertNotNull(result);
        assertFalse(result.isRead());
        verify(messageRepository).save(any());
    }

    @Test
    void sendMessage_noAmigos_lanzaExcepcion() {
        SendMessageRequestDTO dto = mock(SendMessageRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(receiverId);
        when(friendshipRepository.existsByUserIds(senderId, receiverId)).thenReturn(false);

        assertThrows(ConnectionRequestException.class, () -> chatService.sendMessage(senderId, dto));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_autoMensaje_lanzaExcepcion() {
        SendMessageRequestDTO dto = mock(SendMessageRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(senderId);

        assertThrows(ConnectionRequestException.class, () -> chatService.sendMessage(senderId, dto));
        verifyNoInteractions(friendshipRepository, messageRepository);
    }

    @Test
    void getConversation_retornaMensajes() {
        when(messageRepository.findConversation(senderId, receiverId, 0, 20))
                .thenReturn(List.of(buildMessage(UUID.randomUUID(), senderId, receiverId, null)));

        List<ChatMessageResponseDTO> result = chatService.getConversation(senderId, receiverId, 0, 20);

        assertEquals(1, result.size());
    }

    @Test
    void getConversations_retornaResumenPorContacto() {
        UUID msgId = UUID.randomUUID();
        ChatMessage msg1 = buildMessage(msgId, receiverId, senderId, null);
        ChatMessage msg2 = buildMessage(UUID.randomUUID(), senderId, receiverId, LocalDateTime.now());

        when(messageRepository.findAllMessagesForUser(senderId)).thenReturn(List.of(msg1, msg2));
        when(userPresenceRepository.findByUserId(receiverId)).thenReturn(Optional.of(
                UserPresence.builder().userId(receiverId).name("Ana").email("ana@test.com")
                        .lastSeen(LocalDateTime.now().minusMinutes(1)).build()));
        when(messageRepository.countUnread(senderId, receiverId)).thenReturn(1L);

        List<ConversationSummaryResponseDTO> result = chatService.getConversations(senderId);

        assertEquals(1, result.size());
        assertEquals(receiverId, result.get(0).getFriendId());
        assertEquals(1L, result.get(0).getUnreadCount());
    }

    @Test
    void markAsRead_exitoso_actualizaTimestamp() {
        UUID msgId = UUID.randomUUID();
        ChatMessage msg = buildMessage(msgId, senderId, receiverId, null);

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChatMessageResponseDTO result = chatService.markAsRead(msgId, receiverId);

        assertTrue(result.isRead());
        verify(messageRepository).save(any());
    }

    @Test
    void markAsRead_permisoIncorrecto_lanzaExcepcion() {
        UUID msgId = UUID.randomUUID();
        when(messageRepository.findById(msgId)).thenReturn(
                Optional.of(buildMessage(msgId, senderId, receiverId, null)));

        assertThrows(ConnectionRequestException.class,
                () -> chatService.markAsRead(msgId, senderId));
    }

    @Test
    void deleteMessage_porEmisor_marcaDeletedBySender() {
        UUID msgId = UUID.randomUUID();
        ChatMessage msg = buildMessage(msgId, senderId, receiverId, null);

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> chatService.deleteMessage(msgId, senderId));
        verify(messageRepository).save(argThat(m -> m.isDeletedBySender()));
    }

    @Test
    void deleteMessage_porReceptor_marcaDeletedByReceiver() {
        UUID msgId = UUID.randomUUID();
        ChatMessage msg = buildMessage(msgId, senderId, receiverId, null);

        when(messageRepository.findById(msgId)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> chatService.deleteMessage(msgId, receiverId));
        verify(messageRepository).save(argThat(m -> m.isDeletedByReceiver()));
    }

    @Test
    void deleteMessage_sinPermiso_lanzaExcepcion() {
        UUID msgId = UUID.randomUUID();
        when(messageRepository.findById(msgId)).thenReturn(
                Optional.of(buildMessage(msgId, senderId, receiverId, null)));

        assertThrows(ConnectionRequestException.class,
                () -> chatService.deleteMessage(msgId, UUID.randomUUID()));
    }
}
