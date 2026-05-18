package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserPresenceServiceTest {

    @Mock private UserPresenceRepositoryPort presenceRepository;
    @InjectMocks private UserPresenceService userPresenceService;

    private final UUID userId = UUID.randomUUID();

    private UserPresence buildPresence(UUID id, LocalDateTime lastSeen) {
        return UserPresence.builder()
                .id(id).userId(userId)
                .name("Juan").email("juan@test.com")
                .avatarUrl("https://cdn.test.com/avatar.jpg")
                .lastSeen(lastSeen).build();
    }

    @Test
    void heartbeat_newUser_createsPresence() {
        UUID presenceId = UUID.randomUUID();
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(presenceRepository.save(any())).thenReturn(buildPresence(presenceId, LocalDateTime.now()));

        UserPresenceResponseDTO result = userPresenceService.heartbeat(userId, "juan@test.com", "Juan", null);

        assertNotNull(result);
        verify(presenceRepository).save(any());
    }

    @Test
    void heartbeat_existingUser_updatesLastSeen() {
        UUID presenceId = UUID.randomUUID();
        LocalDateTime oldLastSeen = LocalDateTime.now().minusHours(2);
        UserPresence existing = buildPresence(presenceId, oldLastSeen);

        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(presenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userPresenceService.heartbeat(userId, "juan@test.com", "Juan", "https://cdn.test.com/nuevo.jpg");

        ArgumentCaptor<UserPresence> captor = ArgumentCaptor.forClass(UserPresence.class);
        verify(presenceRepository).save(captor.capture());
        assertTrue(captor.getValue().getLastSeen().isAfter(oldLastSeen));
        assertEquals(presenceId, captor.getValue().getId());
        assertEquals("https://cdn.test.com/nuevo.jpg", captor.getValue().getAvatarUrl());
    }

    @Test
    void heartbeat_nullFields_preservesExistingData() {
        UserPresence existing = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(10));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(presenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userPresenceService.heartbeat(userId, null, null, null);

        ArgumentCaptor<UserPresence> captor = ArgumentCaptor.forClass(UserPresence.class);
        verify(presenceRepository).save(captor.capture());
        assertEquals("juan@test.com", captor.getValue().getEmail());
        assertEquals("Juan", captor.getValue().getName());
        assertEquals("https://cdn.test.com/avatar.jpg", captor.getValue().getAvatarUrl());
    }

    @Test
    void getStatus_recentHeartbeat_returnsOnline() {
        UserPresence presence = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(2));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(presence));

        UserPresenceResponseDTO result = userPresenceService.getStatus(userId);

        assertEquals(OnlineStatus.ONLINE, result.getStatus());
        assertEquals(userId, result.getUserId());
        assertEquals("https://cdn.test.com/avatar.jpg", result.getAvatarUrl());
    }

    @Test
    void getStatus_staleHeartbeat_returnsOffline() {
        UserPresence presence = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(10));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(presence));

        assertEquals(OnlineStatus.OFFLINE, userPresenceService.getStatus(userId).getStatus());
    }

    @Test
    void getStatus_userNotFound_returnsOffline() {
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserPresenceResponseDTO result = userPresenceService.getStatus(userId);

        assertEquals(OnlineStatus.OFFLINE, result.getStatus());
        assertEquals(userId, result.getUserId());
        assertNull(result.getLastSeen());
        assertNull(result.getAvatarUrl());
    }
}
