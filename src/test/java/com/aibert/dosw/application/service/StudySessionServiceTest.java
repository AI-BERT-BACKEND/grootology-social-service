package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.exceptions.SessionNotFoundException;
import com.aibert.dosw.domain.model.user.SessionStatus;
import com.aibert.dosw.domain.model.user.StudySession;
import com.aibert.dosw.domain.ports.out.StudySessionRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock private StudySessionRepositoryPort repository;
    @InjectMocks private StudySessionService studySessionService;

    private final UUID creatorId = UUID.randomUUID();
    private final UUID participantId = UUID.randomUUID();

    @Test
    void createSession_validInput_returnsSession() {
        CreateStudySessionRequestDTO request = mock(CreateStudySessionRequestDTO.class);
        when(request.getTopic()).thenReturn("Cálculo diferencial");
        when(request.getScheduledAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(request.getDurationHours()).thenReturn(2.0);
        when(request.getParticipantIds()).thenReturn(List.of(participantId));
        when(request.getNotes()).thenReturn("Traer calculadora");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudySessionResponseDTO response = studySessionService.createSession(creatorId, request);

        assertEquals("Cálculo diferencial", response.getTopic());
        assertEquals(SessionStatus.PENDING, response.getStatus());
        assertTrue(response.getParticipantIds().contains(creatorId));
    }

    @Test
    void respondToSession_accept_changesStatusToAccepted() {
        UUID sessionId = UUID.randomUUID();
        StudySession session = StudySession.builder()
                .id(sessionId).creatorId(creatorId).topic("Álgebra")
                .scheduledAt(LocalDateTime.now().plusDays(1)).durationHours(1.5)
                .participantIds(List.of(participantId)).status(SessionStatus.PENDING).build();

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudySessionResponseDTO response = studySessionService.respondToSession(sessionId, participantId, true);

        assertEquals(SessionStatus.ACCEPTED, response.getStatus());
    }

    @Test
    void respondToSession_reject_changesStatusToRejected() {
        UUID sessionId = UUID.randomUUID();
        StudySession session = StudySession.builder()
                .id(sessionId).creatorId(creatorId).topic("Física")
                .scheduledAt(LocalDateTime.now().plusDays(1)).durationHours(1.0)
                .participantIds(List.of(participantId)).status(SessionStatus.PENDING).build();

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudySessionResponseDTO response = studySessionService.respondToSession(sessionId, participantId, false);

        assertEquals(SessionStatus.REJECTED, response.getStatus());
    }

    @Test
    void respondToSession_sessionNotFound_throwsException() {
        UUID sessionId = UUID.randomUUID();
        when(repository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class,
                () -> studySessionService.respondToSession(sessionId, participantId, true));
    }
}
