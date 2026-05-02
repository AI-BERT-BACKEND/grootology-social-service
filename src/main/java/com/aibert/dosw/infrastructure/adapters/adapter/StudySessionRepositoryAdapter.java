package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.StudySession;
import com.aibert.dosw.domain.ports.out.StudySessionRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.StudySessionPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.StudySessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StudySessionRepositoryAdapter implements StudySessionRepositoryPort {
    private final StudySessionJpaRepository jpaRepository;
    private final StudySessionPersistenceMapper mapper;

    @Override
    public StudySession save(StudySession session) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<StudySession> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<StudySession> findByParticipantId(UUID userId) {
        return jpaRepository.findByParticipantId(userId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }
}
