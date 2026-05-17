package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.ConnectionRequest;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.ports.out.ConnectionRequestRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionRequestServiceTest {

    @Mock private ConnectionRequestRepositoryPort requestRepository;
    @Mock private FriendshipRepositoryPort friendshipRepository;
    @InjectMocks private ConnectionRequestService connectionRequestService;

    private final UUID senderId = UUID.randomUUID();
    private final UUID receiverId = UUID.randomUUID();

    @Test
    void sendRequest_autoSolicitud_lanzaExcepcion() {
        SendConnectionRequestDTO dto = mock(SendConnectionRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(senderId);

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.sendRequest(senderId, dto));
        verifyNoInteractions(friendshipRepository, requestRepository);
    }

    @Test
    void sendRequest_yaAmigos_lanzaExcepcion() {
        SendConnectionRequestDTO dto = mock(SendConnectionRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(receiverId);
        when(friendshipRepository.existsByUserIds(senderId, receiverId)).thenReturn(true);

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.sendRequest(senderId, dto));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void sendRequest_solicitudPendienteEnCualquierDireccion_lanzaExcepcion() {
        SendConnectionRequestDTO dto = mock(SendConnectionRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(receiverId);
        when(friendshipRepository.existsByUserIds(senderId, receiverId)).thenReturn(false);
        when(requestRepository.existsByEitherDirectionAndStatus(senderId, receiverId, ConnectionRequestStatus.PENDING))
                .thenReturn(true);

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.sendRequest(senderId, dto));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void sendRequest_exitoso_guardaSolicitud() {
        SendConnectionRequestDTO dto = mock(SendConnectionRequestDTO.class);
        when(dto.getReceiverId()).thenReturn(receiverId);
        when(friendshipRepository.existsByUserIds(senderId, receiverId)).thenReturn(false);
        when(requestRepository.existsByEitherDirectionAndStatus(senderId, receiverId, ConnectionRequestStatus.PENDING))
                .thenReturn(false);
        when(requestRepository.save(any())).thenReturn(ConnectionRequest.builder()
                .id(UUID.randomUUID()).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build());

        ConnectionRequestResponseDTO result = connectionRequestService.sendRequest(senderId, dto);

        assertNotNull(result);
        assertEquals(ConnectionRequestStatus.PENDING, result.getStatus());
        verify(requestRepository).save(any());
    }

    @Test
    void acceptRequest_creaAmistad() {
        UUID requestId = UUID.randomUUID();
        ConnectionRequest existing = ConnectionRequest.builder()
                .id(requestId).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(requestRepository.save(any())).thenReturn(ConnectionRequest.builder()
                .id(requestId).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.ACCEPTED).sentAt(existing.getSentAt()).build());
        when(friendshipRepository.save(any())).thenReturn(Friendship.builder()
                .id(UUID.randomUUID()).userId1(senderId).userId2(receiverId)
                .createdAt(LocalDateTime.now()).build());

        ConnectionRequestResponseDTO result = connectionRequestService.acceptRequest(requestId, receiverId);

        assertEquals(ConnectionRequestStatus.ACCEPTED, result.getStatus());
        verify(friendshipRepository).save(any());
    }

    @Test
    void acceptRequest_permisoIncorrecto_lanzaExcepcion() {
        UUID requestId = UUID.randomUUID();
        UUID otroUsuario = UUID.randomUUID();
        ConnectionRequest existing = ConnectionRequest.builder()
                .id(requestId).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.acceptRequest(requestId, otroUsuario));
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void rejectRequest_exitoso_noCreaAmistad() {
        UUID requestId = UUID.randomUUID();
        ConnectionRequest existing = ConnectionRequest.builder()
                .id(requestId).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(requestRepository.save(any())).thenReturn(ConnectionRequest.builder()
                .id(requestId).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.REJECTED).sentAt(existing.getSentAt()).build());

        ConnectionRequestResponseDTO result = connectionRequestService.rejectRequest(requestId, receiverId);

        assertEquals(ConnectionRequestStatus.REJECTED, result.getStatus());
        verifyNoInteractions(friendshipRepository);
    }
}
