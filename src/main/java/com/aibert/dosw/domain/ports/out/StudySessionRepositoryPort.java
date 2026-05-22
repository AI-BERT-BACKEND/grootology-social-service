package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.StudySession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudySessionRepositoryPort {
    StudySession save(StudySession session);
    Optional<StudySession> findById(UUID id);
    List<StudySession> findByParticipantId(UUID userId);
    List<StudySession> findPendingInvitesByUserId(UUID userId);
}
