package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.ConnectionRequest;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.ConnectionRequestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConnectionRequestPersistenceMapper {
    ConnectionRequest toDomain(ConnectionRequestEntity entity);
    ConnectionRequestEntity toEntity(ConnectionRequest request);
}
