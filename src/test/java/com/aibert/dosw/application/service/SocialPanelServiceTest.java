package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
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
import com.aibert.dosw.domain.ports.in.ChatUseCase;
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
    @Mock private ChatUseCase chatUseCase;
    @InjectMocks private SocialPanelService socialPanelService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getPanel_noAction_returnsFullPanelWithActions() {
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.ONLINE).lastSeen(LocalDateTime.now()).build());
        when(invitationUseCase.getOrCreateReferralLink(userId)).thenReturn(InviteResponseDTO.builder()
                .referralLink("http://localhost/ref").message("Comparte este enlace").build());
        when(connectionRequestUseCase.getPendingRequestsForUser(userId)).thenReturn(List.of());
        when(availabilityUseCase.getConfig(userId)).thenReturn(AvailabilityConfig.builder()
                .userId(userId).visibility(VisibilityLevel.PUBLIC).build());
        when(studySessionUseCase.getSessionsForUser(userId)).thenReturn(List.of(StudySessionResponseDTO.builder()
                .id(UUID.randomUUID()).topic("Cálculo")
                .scheduledAt(LocalDateTime.now().plusDays(1)).durationHours(2.0)
                .participantIds(List.of(userId)).build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, null);

        assertEquals(SocialAction.LISTAR, response.getSelectedAction());
        assertEquals(4, response.getAvailableActions().size());
        assertNotNull(response.getPresenceStatus());
        assertEquals(OnlineStatus.ONLINE, response.getPresenceStatus().getStatus());
        assertNotNull(response.getInvite());
        assertNotNull(response.getPendingConnectionRequests());
        assertNotNull(response.getAvailability());
        assertEquals(1, response.getSessions().size());
        assertNull(response.getRecentConversations());
    }

    @Test
    void getPanel_inviteAction_returnsPendingRequests() {
        UUID requestId = UUID.randomUUID();
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build());
        when(invitationUseCase.getOrCreateReferralLink(userId)).thenReturn(InviteResponseDTO.builder()
                .referralLink("http://localhost/ref").message("msg").build());
        when(connectionRequestUseCase.getPendingRequestsForUser(userId)).thenReturn(List.of(
                ConnectionRequestResponseDTO.builder()
                        .id(requestId).senderId(UUID.randomUUID()).receiverId(userId)
                        .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.INVITAR);

        assertEquals(SocialAction.INVITAR, response.getSelectedAction());
        assertEquals(1, response.getPendingConnectionRequests().size());
        assertNull(response.getAvailability());
        assertNull(response.getSessions());
        assertNull(response.getRecentConversations());
    }

    @Test
    void getPanel_searchAction_returnsAvailability() {
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build());
        when(availabilityUseCase.getConfig(userId)).thenReturn(AvailabilityConfig.builder()
                .userId(userId).visibility(VisibilityLevel.PUBLIC).build());

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.BUSCAR);

        assertEquals(SocialAction.BUSCAR, response.getSelectedAction());
        assertNotNull(response.getAvailability());
        assertNull(response.getInvite());
        assertNull(response.getSessions());
        assertNull(response.getRecentConversations());
        verifyNoInteractions(invitationUseCase, connectionRequestUseCase, studySessionUseCase, chatUseCase);
    }

    @Test
    void getPanel_listAction_returnsSessions() {
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build());
        when(studySessionUseCase.getSessionsForUser(userId)).thenReturn(List.of(
                StudySessionResponseDTO.builder().id(UUID.randomUUID()).topic("Álgebra")
                        .scheduledAt(LocalDateTime.now().plusDays(2)).durationHours(1.5)
                        .participantIds(List.of(userId)).build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.LISTAR);

        assertEquals(SocialAction.LISTAR, response.getSelectedAction());
        assertEquals(1, response.getSessions().size());
        assertNull(response.getInvite());
        assertNull(response.getAvailability());
        assertNull(response.getRecentConversations());
        verifyNoInteractions(invitationUseCase, connectionRequestUseCase, availabilityUseCase, chatUseCase);
    }

    @Test
    void getPanel_chatAction_returnsConversations() {
        UUID friendId = UUID.randomUUID();
        when(userPresenceUseCase.getStatus(userId)).thenReturn(UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.ONLINE).build());
        when(chatUseCase.getConversations(userId)).thenReturn(List.of(
                ConversationSummaryResponseDTO.builder()
                        .friendId(friendId).friendName("Ana")
                        .lastMessageContent("Hola!")
                        .lastMessageSenderId(friendId)
                        .lastMessageAt(LocalDateTime.now().minusMinutes(5))
                        .unreadCount(2).build()));

        SocialPanelResponseDTO response = socialPanelService.getPanel(userId, SocialAction.CHAT);

        assertEquals(SocialAction.CHAT, response.getSelectedAction());
        assertNotNull(response.getRecentConversations());
        assertEquals(1, response.getRecentConversations().size());
        assertEquals(2, response.getRecentConversations().get(0).getUnreadCount());
        assertNull(response.getInvite());
        assertNull(response.getSessions());
        verifyNoInteractions(invitationUseCase, availabilityUseCase, studySessionUseCase, connectionRequestUseCase);
    }
}
