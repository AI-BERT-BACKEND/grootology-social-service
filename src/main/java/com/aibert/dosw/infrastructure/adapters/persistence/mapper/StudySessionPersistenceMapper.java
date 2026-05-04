package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.StudySession;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.StudySessionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudySessionPersistenceMapper {
    StudySession toDomain(StudySessionEntity entity);
    StudySessionEntity toEntity(StudySession session);
}
