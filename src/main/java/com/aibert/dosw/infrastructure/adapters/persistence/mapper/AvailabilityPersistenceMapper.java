package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.AvailabilityConfigEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvailabilityPersistenceMapper {
    AvailabilityConfig toDomain(AvailabilityConfigEntity entity);
    AvailabilityConfigEntity toEntity(AvailabilityConfig config);
}
