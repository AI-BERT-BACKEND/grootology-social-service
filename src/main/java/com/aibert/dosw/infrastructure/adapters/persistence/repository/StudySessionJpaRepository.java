package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.StudySessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface StudySessionJpaRepository extends JpaRepository<StudySessionEntity, UUID> {
    @Query("SELECT s FROM StudySessionEntity s WHERE :userId MEMBER OF s.participantIds")
    List<StudySessionEntity> findByParticipantId(UUID userId);

    @Query("SELECT s FROM StudySessionEntity s WHERE :userId MEMBER OF s.participantIds AND s.creatorId <> :userId AND s.status = 'PENDING'")
    List<StudySessionEntity> findPendingInvitesByUserId(UUID userId);
}
