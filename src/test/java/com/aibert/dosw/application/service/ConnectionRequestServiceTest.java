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
import java.util.List;
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

    private ConnectionRequest buildRequest(UUID id, ConnectionRequestStatus status) {
        return ConnectionRequest.builder()
                .id(id).senderId(senderId).receiverId(receiverId)
                .status(status).sentAt(LocalDateTime.now()).build();
    }

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
        when(requestRepository.save(any())).thenReturn(buildRequest(UUID.randomUUID(), ConnectionRequestStatus.PENDING));

        ConnectionRequestResponseDTO result = connectionRequestService.sendRequest(senderId, dto);

        assertNotNull(result);
        assertEquals(ConnectionRequestStatus.PENDING, result.getStatus());
        verify(requestRepository).save(any());
    }

    @Test
    void acceptRequest_creaAmistad() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.PENDING)));
        when(requestRepository.save(any())).thenReturn(buildRequest(requestId, ConnectionRequestStatus.ACCEPTED));
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
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.PENDING)));

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.acceptRequest(requestId, UUID.randomUUID()));
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void acceptRequest_yaFueProcesada_lanzaExcepcion() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.ACCEPTED)));

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.acceptRequest(requestId, receiverId));
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void rejectRequest_exitoso_noCreaAmistad() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.PENDING)));
        when(requestRepository.save(any())).thenReturn(buildRequest(requestId, ConnectionRequestStatus.REJECTED));

        ConnectionRequestResponseDTO result = connectionRequestService.rejectRequest(requestId, receiverId);

        assertEquals(ConnectionRequestStatus.REJECTED, result.getStatus());
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void rejectRequest_permisoIncorrecto_lanzaExcepcion() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.PENDING)));

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.rejectRequest(requestId, UUID.randomUUID()));
    }

    @Test
    void rejectRequest_yaFueProcesada_lanzaExcepcion() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(buildRequest(requestId, ConnectionRequestStatus.REJECTED)));

        assertThrows(ConnectionRequestException.class,
                () -> connectionRequestService.rejectRequest(requestId, receiverId));
    }

    @Test
    void getPendingRequestsForUser_retornaLista() {
        when(requestRepository.findByReceiverIdAndStatus(receiverId, ConnectionRequestStatus.PENDING))
                .thenReturn(List.of(buildRequest(UUID.randomUUID(), ConnectionRequestStatus.PENDING)));

        List<ConnectionRequestResponseDTO> result = connectionRequestService.getPendingRequestsForUser(receiverId);

        assertEquals(1, result.size());
        assertEquals(ConnectionRequestStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void getSentRequestsByUser_retornaLista() {
        when(requestRepository.findBySenderId(senderId))
                .thenReturn(List.of(
                        buildRequest(UUID.randomUUID(), ConnectionRequestStatus.PENDING),
                        buildRequest(UUID.randomUUID(), ConnectionRequestStatus.ACCEPTED)));

        List<ConnectionRequestResponseDTO> result = connectionRequestService.getSentRequestsByUser(senderId);

        assertEquals(2, result.size());
    }
}
