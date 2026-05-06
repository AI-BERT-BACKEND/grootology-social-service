package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.exceptions.SessionNotFoundException;
import com.aibert.dosw.domain.model.user.SessionStatus;
import com.aibert.dosw.domain.model.user.StudySession;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import com.aibert.dosw.domain.ports.out.StudySessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudySessionService implements StudySessionUseCase {

    private final StudySessionRepositoryPort repository;

    @Override
    public StudySessionResponseDTO createSession(UUID creatorId, CreateStudySessionRequestDTO request) {
        List<UUID> participants = new ArrayList<>(request.getParticipantIds());
        participants.add(creatorId);

        StudySession session = repository.save(StudySession.builder()
                .id(UUID.randomUUID())
                .creatorId(creatorId)
                .topic(request.getTopic())
                .scheduledAt(request.getScheduledAt())
                .durationHours(request.getDurationHours())
                .notes(request.getNotes())
                .participantIds(participants)
                .status(SessionStatus.PENDING)
                .build());

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
