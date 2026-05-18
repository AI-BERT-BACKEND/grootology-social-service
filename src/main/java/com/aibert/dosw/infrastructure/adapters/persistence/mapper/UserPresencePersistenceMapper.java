package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.UserPresenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPresencePersistenceMapper {
    UserPresence toDomain(UserPresenceEntity entity);
    UserPresenceEntity toEntity(UserPresence presence);
}
