package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.RelationshipStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.out.ConnectionRequestRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest {

    @Mock private UserPresenceRepositoryPort userPresenceRepository;
    @Mock private FriendshipRepositoryPort friendshipRepository;
    @Mock private ConnectionRequestRepositoryPort connectionRequestRepository;
    @InjectMocks private UserSearchService userSearchService;

    private final UUID requesterId = UUID.randomUUID();
    private final UUID targetId = UUID.randomUUID();

    private UserPresence buildPresence(UUID uid) {
        return UserPresence.builder()
                .id(UUID.randomUUID()).userId(uid)
                .name("Nombre").email("correo@test.com")
                .lastSeen(LocalDateTime.now().minusMinutes(2)).build();
    }

    @Test
    void search_queryNula_retornaVacio() {
        List<UserSearchResultDTO> result = userSearchService.search(null, requesterId);
        assertTrue(result.isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_queryBlanca_retornaVacio() {
        List<UserSearchResultDTO> result = userSearchService.search("   ", requesterId);
        assertTrue(result.isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_sinRelacion_retornaEstadoNone() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                targetId, requesterId, ConnectionRequestStatus.PENDING)).thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(1, result.size());
        assertEquals(RelationshipStatus.NONE, result.get(0).getRelationshipStatus());
        assertEquals(targetId, result.get(0).getUserId());
    }

    @Test
    void search_yaAmigo_retornaFriend() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(true);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(RelationshipStatus.FRIEND, result.get(0).getRelationshipStatus());
        verify(connectionRequestRepository, never()).existsBySenderIdAndReceiverIdAndStatus(any(), any(), any());
    }

    @Test
    void search_solicitudEnviada_retornaPendingSent() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(true);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(RelationshipStatus.PENDING_SENT, result.get(0).getRelationshipStatus());
    }

    @Test
    void search_solicitudRecibida_retornaPendingReceived() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                targetId, requesterId, ConnectionRequestStatus.PENDING)).thenReturn(true);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(RelationshipStatus.PENDING_RECEIVED, result.get(0).getRelationshipStatus());
    }

    @Test
    void search_multipleResultados_procesaTodos() {
        UUID otro = UUID.randomUUID();
        when(userPresenceRepository.searchByNameOrEmail("correo", requesterId))
                .thenReturn(List.of(buildPresence(targetId), buildPresence(otro)));
        when(friendshipRepository.existsByUserIds(any(), any())).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("correo", requesterId);

        assertEquals(2, result.size());
    }
}
