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
                .name("Name").email("email@test.com")
                .lastSeen(LocalDateTime.now().minusMinutes(2)).build();
    }

    @Test
    void search_nullQuery_returnsEmpty() {
        assertTrue(userSearchService.search(null, requesterId).isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_blankQuery_returnsEmpty() {
        assertTrue(userSearchService.search("   ", requesterId).isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void search_publicUser_appearsInResults() {
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
    void search_privateUser_notInResults() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PRIVATE).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);

        assertTrue(userSearchService.search("juan", requesterId).isEmpty());
    }

    @Test
    void search_privateUserButFriend_appearsInResults() {
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
    void search_specificUser_appearsForAuthorized() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId)
                        .visibility(VisibilityLevel.SPECIFIC)
                        .authorizedFriends(List.of(requesterId)).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        assertEquals(1, userSearchService.search("juan", requesterId).size());
    }

    @Test
    void search_specificUser_hiddenForUnauthorized() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId)
                        .visibility(VisibilityLevel.SPECIFIC)
                        .authorizedFriends(List.of(UUID.randomUUID())).build()));

        assertTrue(userSearchService.search("juan", requesterId).isEmpty());
    }

    @Test
    void search_noVisibilityConfig_treatedAsPublic() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.empty());
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                .thenReturn(false);

        assertEquals(1, userSearchService.search("juan", requesterId).size());
    }

    @Test
    void search_sentRequest_returnsPendingSent() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PUBLIC).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(true);

        assertEquals(RelationshipStatus.PENDING_SENT, userSearchService.search("juan", requesterId).get(0).getRelationshipStatus());
    }

    @Test
    void search_receivedRequest_returnsPendingReceived() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId).visibility(VisibilityLevel.PUBLIC).build()));
        when(friendshipRepository.existsByUserIds(requesterId, targetId)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                requesterId, targetId, ConnectionRequestStatus.PENDING)).thenReturn(false);
        when(connectionRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                targetId, requesterId, ConnectionRequestStatus.PENDING)).thenReturn(true);

        assertEquals(RelationshipStatus.PENDING_RECEIVED, userSearchService.search("juan", requesterId).get(0).getRelationshipStatus());
    }

    @Test
    void search_specificUserNullAuthorizedList_notInResults() {
        when(userPresenceRepository.searchByNameOrEmail("juan", requesterId))
                .thenReturn(List.of(buildPresence(targetId)));
        when(availabilityRepository.findByUserId(targetId)).thenReturn(Optional.of(
                AvailabilityConfig.builder().userId(targetId)
                        .visibility(VisibilityLevel.SPECIFIC)
                        .authorizedFriends(null).build()));

        assertTrue(userSearchService.search("juan", requesterId).isEmpty());
    }
}
