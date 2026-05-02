package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import java.util.List;
import java.util.UUID;

public interface StudySessionUseCase {
    StudySessionResponseDTO createSession(UUID creatorId, CreateStudySessionRequestDTO request);
    StudySessionResponseDTO respondToSession(UUID sessionId, UUID userId, boolean accept);
    List<StudySessionResponseDTO> getSessionsForUser(UUID userId);
}
