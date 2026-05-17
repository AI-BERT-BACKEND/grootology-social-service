package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
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
class SocialPanelServiceTest {

    @Mock private InvitationUseCase invitationUseCase;
    @Mock private AvailabilityUseCase availabilityUseCase;
    @Mock private StudySessionUseCase studySessionUseCase;
    @Mock private UserPresenceUseCase userPresenceUseCase;
    @Mock private ConnectionRequestUseCase connectionRequestUseCase;
    @InjectMocks private SocialPanelService socialPanelService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getPanel_general_retornaResumenConAcciones() {
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.ONLINE).lastSeen(LocalDateTime.now()).build());
        when(invitationUseCase.getOrCreateReferralLink(userId)).thenReturn(InviteResponseDTO.builder()
                .referralLink("http://localhost/ref")
                .message("Comparte este enlace con usuarios externos a AI.BERT")
                .build());
        when(connectionRequestUseCase.getPendingRequestsForUser(userId)).thenReturn(List.of());
        when(availabilityUseCase.getConfig(userId)).thenReturn(AvailabilityConfig.builder()
                .userId(userId)
                .visibility(VisibilityLevel.PUBLIC)
                .build());
        when(studySessionUseCase.getSessionsForUser(userId)).thenReturn(List.of(StudySessionResponseDTO.builder()
                .id(UUID.randomUUID())
                .topic("Calculo")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationHours(2.0)
                .participantIds(List.of(userId))
                .build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, null);

        assertEquals(SocialAction.LISTAR, response.getSelectedAction());
        assertEquals(4, response.getAvailableActions().size());
        assertNotNull(response.getPresenceStatus());
        assertEquals(OnlineStatus.ONLINE, response.getPresenceStatus().getStatus());
        assertNotNull(response.getInvite());
        assertNotNull(response.getPendingConnectionRequests());
        assertNotNull(response.getAvailability());
        assertEquals(1, response.getSessions().size());
        assertNull(response.getChatStatus());
    }

    @Test
    void getPanel_invitar_retornaSolicitudesPendientes() {
        UUID requestId = UUID.randomUUID();
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build());
        when(invitationUseCase.getOrCreateReferralLink(userId)).thenReturn(InviteResponseDTO.builder()
                .referralLink("http://localhost/ref").message("msg").build());
        when(connectionRequestUseCase.getPendingRequestsForUser(userId)).thenReturn(List.of(
                ConnectionRequestResponseDTO.builder()
                        .id(requestId)
                        .senderId(UUID.randomUUID())
                        .receiverId(userId)
                        .status(ConnectionRequestStatus.PENDING)
                        .sentAt(LocalDateTime.now())
                        .build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.INVITAR);

        assertEquals(SocialAction.INVITAR, response.getSelectedAction());
        assertEquals(1, response.getPendingConnectionRequests().size());
        assertNull(response.getAvailability());
        assertNull(response.getSessions());
        assertNull(response.getChatStatus());
    }

    @Test
    void getPanel_chat_retornaEstadoChatSinConsultarOtrosModulos() {
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build());

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.CHAT);

        assertEquals(SocialAction.CHAT, response.getSelectedAction());
        assertEquals("CHAT_PENDING_BACKEND_INTEGRATION", response.getChatStatus());
        assertNotNull(response.getPresenceStatus());
        assertNull(response.getInvite());
        assertNull(response.getPendingConnectionRequests());
        assertNull(response.getAvailability());
        assertNull(response.getSessions());
        verifyNoInteractions(invitationUseCase, availabilityUseCase, studySessionUseCase, connectionRequestUseCase);
    }
}
