package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.exceptions.SessionNotFoundException;
import com.aibert.dosw.domain.model.user.SessionStatus;
import com.aibert.dosw.domain.model.user.StudySession;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import com.aibert.dosw.domain.ports.out.NotificationPublisherPort;
import com.aibert.dosw.domain.ports.out.StudySessionRepositoryPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudySessionService implements StudySessionUseCase {

    private final StudySessionRepositoryPort repository;
    private final NotificationPublisherPort notificationPublisher;

    @Override
    public StudySessionResponseDTO createSession(UUID creatorId, CreateStudySessionRequestDTO request) {
        List<UUID> participants = new ArrayList<>(request.getParticipantIds());
        participants.add(creatorId);

        // Sin .id(...): se deja en null para que JPA haga INSERT y genere el id (@GeneratedValue).
        StudySession session = repository.save(StudySession.builder()
                .creatorId(creatorId)
                .topic(request.getTopic())
                .scheduledAt(request.getScheduledAt())
                .durationHours(request.getDurationHours())
                .notes(request.getNotes())
                .participantIds(participants)
                .status(SessionStatus.PENDING)
                .build());

        notifyInvitedParticipants(session, request.getParticipantIds());

        return toDTO(session);
    }

    @Override
    public StudySessionResponseDTO respondToSession(UUID sessionId, UUID userId, boolean accept) {
        StudySession session = repository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);

        SessionStatus newStatus = accept ? SessionStatus.ACCEPTED : SessionStatus.REJECTED;

        StudySession updated = StudySession.builder()
                .id(session.getId())
                .creatorId(session.getCreatorId())
                .topic(session.getTopic())
                .scheduledAt(session.getScheduledAt())
                .durationHours(session.getDurationHours())
                .notes(session.getNotes())
                .participantIds(session.getParticipantIds())
                .status(newStatus)
                .build();

        return toDTO(repository.save(updated));
    }

    @Override
    public List<StudySessionResponseDTO> getSessionsForUser(UUID userId) {
        return repository.findByParticipantId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudySessionResponseDTO> getPendingInvitesForUser(UUID userId) {
        return repository.findPendingInvitesByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void notifyInvitedParticipants(StudySession session, List<UUID> invitedIds) {
        for (UUID participantId : invitedIds) {
            try {
                notificationPublisher.publish(NotificationEvent.builder()
                        .userId(participantId)
                        .type("STUDY_SESSION_INVITE")
                        .title("Nueva invitacion a sesion de estudio")
                        .message("Te han invitado a una sesion de estudio sobre: " + session.getTopic())
                        .severity("INFO")
                        .relatedEntityId(session.getId())
                        .build());
            } catch (Exception e) {
                log.error("No se pudo publicar notificacion para participantId={} sessionId={}: {}",
                        participantId, session.getId(), e.getMessage());
            }
        }
    }

    private StudySessionResponseDTO toDTO(StudySession session) {
        return StudySessionResponseDTO.builder()
                .id(session.getId())
                .topic(session.getTopic())
                .scheduledAt(session.getScheduledAt())
                .durationHours(session.getDurationHours())
                .participantIds(session.getParticipantIds())
                .status(session.getStatus())
                .notes(session.getNotes())
                .build();
    }
}
