package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.RelationshipStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.out.AvailabilityRepositoryPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest {

    @Mock private UserPresenceRepositoryPort userPresenceRepository;
    @Mock private FriendshipRepositoryPort friendshipRepository;
    @Mock private ConnectionRequestRepositoryPort connectionRequestRepository;
    @Mock private AvailabilityRepositoryPort availabilityRepository;
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
        assertTrue(userSearchService.search(null, requesterId).isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_queryBlanca_retornaVacio() {
        assertTrue(userSearchService.search("   ", requesterId).isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_usuarioPublico_aparecEnResultados() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PUBLIC).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(1, result.size());
        assertEquals(RelationshipStatus.NONE, result.get(0).getRelationshipStatus());
    }

    @Test
    void search_usuarioPrivado_noApareceEnResultados() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PRIVATE).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertTrue(result.isEmpty());
    }

    @Test
    void search_usuarioPrivadoPeroAmigo_apareceEnResultados() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PRIVATE).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(true);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(1, result.size());
        assertEquals(RelationshipStatus.FRIEND, result.get(0).getRelationshipStatus());
    }

    @Test
    void search_usuarioSpecific_apareceParaAutorizado() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId)
                        .visibility(VisibilityLevel.SPECIFIC)
                        .authorizedFriends(List.of(requesterId)).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(1, result.size());
    }

    @Test
    void search_usuarioSpecific_noApareceParaNoAutorizado() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId)
                        .visibility(VisibilityLevel.SPECIFIC)
                        .authorizedFriends(List.of(UUID.randomUUID())).build()));

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertTrue(result.isEmpty());
    }

    @Test
    void search_sinConfiguracion_trataComoPublico() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.empty());
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(1, result.size());
    }

    @Test
    void search_solicitudEnviada_retornaPendingSent() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PUBLIC).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(true);

        List<UserSearchResultDTO> result = userSearchService.search("juan", requesterId);

        assertEquals(RelationshipStatus.PENDING_SENT, result.get(0).getRelationshipStatus());
    }
}
