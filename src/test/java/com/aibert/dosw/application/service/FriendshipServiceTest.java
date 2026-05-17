package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
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
                .name("Nombre").email("amigo@test.com")
                .lastSeen(lastSeen).build();
    }

    @Test
    void listFriends_sinFiltro_retornaTodasLasAmistades() {
        Friendship f = buildFriendship(userId, friendId);
        UserPresence presence = buildPresence(friendId, LocalDateTime.now().minusMinutes(2));

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.of(presence));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
        assertEquals(OnlineStatus.ONLINE, result.get(0).getPresenceStatus().getStatus());
        assertEquals("Nombre", result.get(0).getFriendName());
    }

    @Test
    void listFriends_usuarioEsUserId2_resolveAmigoCorrecto() {
        Friendship f = buildFriendship(friendId, userId);
        UserPresence presence = buildPresence(friendId, LocalDateTime.now().minusMinutes(1));

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.of(presence));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
    }

    @Test
    void listFriends_filtroOnline_retornaSoloOnline() {
        UUID friendOffline = UUID.randomUUID();
        Friendship f1 = buildFriendship(userId, friendId);
        Friendship f2 = buildFriendship(userId, friendOffline);

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f1, f2));
        when(userPresenceRepository.findByUserId(friendId))
                .thenReturn(Optional.of(buildPresence(friendId, LocalDateTime.now().minusMinutes(1))));
        when(userPresenceRepository.findByUserId(friendOffline))
                .thenReturn(Optional.of(buildPresence(friendOffline, LocalDateTime.now().minusHours(1))));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, OnlineStatus.ONLINE);

        assertEquals(1, result.size());
        assertEquals(friendId, result.get(0).getFriendId());
    }

    @Test
    void listFriends_filtroOffline_retornaSoloOffline() {
        UUID friendOffline = UUID.randomUUID();
        Friendship f1 = buildFriendship(userId, friendId);
        Friendship f2 = buildFriendship(userId, friendOffline);

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f1, f2));
        when(userPresenceRepository.findByUserId(friendId))
                .thenReturn(Optional.of(buildPresence(friendId, LocalDateTime.now().minusMinutes(1))));
        when(userPresenceRepository.findByUserId(friendOffline))
                .thenReturn(Optional.of(buildPresence(friendOffline, LocalDateTime.now().minusHours(1))));

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, OnlineStatus.OFFLINE);

        assertEquals(1, result.size());
        assertEquals(friendOffline, result.get(0).getFriendId());
    }

    @Test
    void listFriends_sinPresenciaRegistrada_retornaOffline() {
        Friendship f = buildFriendship(userId, friendId);

        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(f));
        when(userPresenceRepository.findByUserId(friendId)).thenReturn(Optional.empty());

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertEquals(1, result.size());
        assertEquals(OnlineStatus.OFFLINE, result.get(0).getPresenceStatus().getStatus());
        assertNull(result.get(0).getFriendName());
        assertNull(result.get(0).getFriendEmail());
    }

    @Test
    void listFriends_listaVacia_retornaVacio() {
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of());

        List<FriendshipResponseDTO> result = friendshipService.listFriends(userId, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(userPresenceRepository);
    }
}
