package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.SocialActionItemResponseDTO;
import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.aibert.dosw.domain.ports.in.SocialPanelUseCase;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialPanelService implements SocialPanelUseCase {

    private final InvitationUseCase invitationUseCase;
    private final AvailabilityUseCase availabilityUseCase;
    private final StudySessionUseCase studySessionUseCase;
    private final UserPresenceUseCase userPresenceUseCase;
    private final ConnectionRequestUseCase connectionRequestUseCase;

    @Override
    public SocialPanelResponseDTO getPanel(UUID userId, SocialAction action) {
        SocialAction selectedAction = action != null ? action : SocialAction.LISTAR;

        return SocialPanelResponseDTO.builder()
                .userId(userId)
                .selectedAction(selectedAction)
                .availableActions(buildAvailableActions(userId))
                .presenceStatus(userPresenceUseCase.getStatus(userId))
                .invite(shouldIncludeInvite(action) ? invitationUseCase.getOrCreateReferralLink(userId) : null)
                .pendingConnectionRequests(shouldIncludeConnectionRequests(action)
                        ? connectionRequestUseCase.getPendingRequestsForUser(userId) : null)
                .availability(shouldIncludeAvailability(action) ? availabilityUseCase.getConfig(userId) : null)
                .sessions(shouldIncludeSessions(action) ? studySessionUseCase.getSessionsForUser(userId) : null)
                .chatStatus(shouldIncludeChat(action) ? "CHAT_PENDING_BACKEND_INTEGRATION" : null)
                .build();
    }

    private List<SocialActionItemResponseDTO> buildAvailableActions(UUID userId) {
        return List.of(
                SocialActionItemResponseDTO.builder()
                        .action(SocialAction.INVITAR)
                        .label("Invitar")
                        .endpoint("/api/social/connections/" + userId + "/request")
                        .method("POST")
                        .description("Enviar solicitud de conexión a un usuario registrado en AI.BERT.")
                        .build(),
                SocialActionItemResponseDTO.builder()
                        .action(SocialAction.BUSCAR)
                        .label("Buscar")
                        .endpoint("/api/social/users/search?requesterId=" + userId + "&q=")
                        .method("GET")
                        .description("Búsqueda de usuarios registrados en AI.BERT por nombre o correo institucional.")
                        .build(),
                SocialActionItemResponseDTO.builder()
                        .action(SocialAction.LISTAR)
                        .label("Listar")
                        .endpoint("/api/social/sessions/user/" + userId)
                        .method("GET")
                        .description("Listado de sesiones sociales asociadas al usuario.")
                        .build(),
                SocialActionItemResponseDTO.builder()
                        .action(SocialAction.CHAT)
                        .label("Chat")
                        .endpoint("/api/social/users/" + userId + "/panel?action=CHAT")
                        .method("GET")
                        .description("Estado del modulo de chat social.")
                        .build()
        );
    }

    private boolean shouldIncludeInvite(SocialAction action) {
        return action == null || action == SocialAction.INVITAR;
    }

    private boolean shouldIncludeConnectionRequests(SocialAction action) {
        return action == null || action == SocialAction.INVITAR;
    }

    private boolean shouldIncludeAvailability(SocialAction action) {
        return action == null || action == SocialAction.BUSCAR;
    }

    private boolean shouldIncludeSessions(SocialAction action) {
        return action == null || action == SocialAction.LISTAR;
    }

    private boolean shouldIncludeChat(SocialAction action) {
        return action == SocialAction.CHAT;
    }
}
