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
                .lastSeen(lastSeen).build();
    }

    @Test
    void heartbeat_usuarioNuevo_creaPresencia() {
        UUID presenceId = UUID.randomUUID();
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(presenceRepository.save(any())).thenReturn(
                buildPresence(presenceId, LocalDateTime.now()));

        UserPresenceResponseDTO result = userPresenceService.heartbeat(userId, "juan@test.com", "Juan");

        assertNotNull(result);
        verify(presenceRepository).save(any());
    }

    @Test
    void heartbeat_usuarioExistente_actualizaLastSeen() {
        UUID presenceId = UUID.randomUUID();
        LocalDateTime oldLastSeen = LocalDateTime.now().minusHours(2);
        UserPresence existing = buildPresence(presenceId, oldLastSeen);

        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(presenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userPresenceService.heartbeat(userId, "juan@test.com", "Juan");

        ArgumentCaptor<UserPresence> captor = ArgumentCaptor.forClass(UserPresence.class);
        verify(presenceRepository).save(captor.capture());
        assertTrue(captor.getValue().getLastSeen().isAfter(oldLastSeen));
        assertEquals(presenceId, captor.getValue().getId());
    }

    @Test
    void heartbeat_emailNulo_conservaEmailExistente() {
        UserPresence existing = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(10));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(presenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userPresenceService.heartbeat(userId, null, null);

        ArgumentCaptor<UserPresence> captor = ArgumentCaptor.forClass(UserPresence.class);
        verify(presenceRepository).save(captor.capture());
        assertEquals("juan@test.com", captor.getValue().getEmail());
        assertEquals("Juan", captor.getValue().getName());
    }

    @Test
    void getStatus_usuarioOnline_retornaOnline() {
        UserPresence presence = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(2));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(presence));

        UserPresenceResponseDTO result = userPresenceService.getStatus(userId);

        assertEquals(OnlineStatus.ONLINE, result.getStatus());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void getStatus_usuarioOffline_retornaOffline() {
        UserPresence presence = buildPresence(UUID.randomUUID(), LocalDateTime.now().minusMinutes(10));
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.of(presence));

        UserPresenceResponseDTO result = userPresenceService.getStatus(userId);

        assertEquals(OnlineStatus.OFFLINE, result.getStatus());
    }

    @Test
    void getStatus_usuarioNoExistente_retornaOffline() {
        when(presenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserPresenceResponseDTO result = userPresenceService.getStatus(userId);

        assertEquals(OnlineStatus.OFFLINE, result.getStatus());
        assertEquals(userId, result.getUserId());
        assertNull(result.getLastSeen());
    }
}
