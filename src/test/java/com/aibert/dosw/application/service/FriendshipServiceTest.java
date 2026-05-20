package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
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
class FriendshipServiceTest {

    @Mock private FriendshipRepositoryPort friendshipRepository;
    @Mock private UserPresenceRepositoryPort userPresenceRepository;
    @InjectMocks private FriendshipService friendshipService;

    private final UUID userId = UUID.randomUUID();
    private final UUID friendId = UUID.randomUUID();

    private Friendship buildFriendship(UUID uid1, UUID uid2) {
        return Friendship.builder()
                .id(UUID.randomUUID()).userId1(uid1).userId2(uid2)
                .createdAt(LocalDateTime.now().minusDays(1)).build();
    }

    private UserPresence buildPresence(UUID uid, LocalDateTime lastSeen) {
        return UserPresence.builder()
                .id(UUID.randomUUID()).userId(uid)
                .name("Friend Name").email("friend@test.com")
                .avatarUrl("https://cdn.test.com/avatar.jpg")
                .lastSeen(lastSeen).build();
    }

    @Test
    void listFriends_noFilter_returnsAllFriends() {
        Friendship f = buildFriendship(userId, friendId);
        UserPresence presence = buildPresence(friendId, LocalDateTime.now().minusMinutes(2));

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.of(presence));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
        assertEquals(OnlineStatus.ONLINE, result.get(0).getPresenceStatus().getStatus());
        assertEquals("Friend Name", result.get(0).getFriendName());
        assertEquals("https://cdn.test.com/avatar.jpg", result.get(0).getFriendAvatarUrl());
    }

    @Test
    void listFriends_userIsUserId2_resolvesCorrectFriend() {
        Friendship f = buildFriendship(friendId, userId);
        UserPresence presence = buildPresence(friendId, LocalDateTime.now().minusMinutes(1));

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.of(presence));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
    }

    @Test
    void listFriends_onlineFilter_returnsOnlyOnline() {
        UUID offlineFriend = UUID.randomUUID();
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(
                buildFriendship(userId, friendId),
                buildFriendship(userId, offlineFriend)));
        when(userPresenceRepository.findByUserId(friendId))
                .thenReturn(Optional.of(buildPresence(friendId, LocalDateTime.now().minusMinutes(1))));
        when(userPresenceRepository.findByUserId(offlineFriend))
                .thenReturn(Optional.of(buildPresence(offlineFriend, LocalDateTime.now().minusHours(1))));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, OnlineStatus.ONLINE);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
    }

    @Test
    void listFriends_offlineFilter_returnsOnlyOffline() {
        UUID offlineFriend = UUID.randomUUID();
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(
                buildFriendship(userId, friendId),
                buildFriendship(userId, offlineFriend)));
        when(userPresenceRepository.findByUserId(friendId))
                .thenReturn(Optional.of(buildPresence(friendId, LocalDateTime.now().minusMinutes(1))));
        when(userPresenceRepository.findByUserId(offlineFriend))
                .thenReturn(Optional.of(buildPresence(offlineFriend, LocalDateTime.now().minusHours(1))));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, OnlineStatus.OFFLINE);

        assertEquals(1, result.size());
        assertEquals(offlineFriend, result.get(0).getFriendId());
    }

    @Test
    void listFriends_noPresence_returnsOfflineWithNullAvatar() {
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(buildFriendship(userId, friendId)));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.empty());

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(OnlineStatus.OFFLINE, result.get(0).getPresenceStatus().getStatus());
        assertNull(result.get(0).getFriendAvatarUrl());
    }

    @Test
    void listFriends_noFriendships_returnsEmpty() {
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of());

        assertTrue(friendshipService.listFriends(userId, null).isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }

    @Test
    void removeFriend_friendshipExists_removes() {
        when(friendshipRepository.existsByUserIds(userId, friendId)).thenReturn(true);
        doNothing().when(friendshipRepository).deleteByUserIds(userId, friendId);

        assertDoesNotThrow(() -> friendshipService.removeFriend(userId, friendId));
        verify(friendshipRepository).deleteByUserIds(userId, friendId);
    }

    @Test
    void removeFriend_noFriendship_throwsException() {
        when(friendshipRepository.existsByUserIds(userId, friendId)).thenReturn(false);

        assertThrows(ConnectionRequestException.class,
                () -> friendshipService.removeFriend(userId, friendId));
        verify(friendshipRepository, never()).deleteByUserIds(any(), any());
    }
}
