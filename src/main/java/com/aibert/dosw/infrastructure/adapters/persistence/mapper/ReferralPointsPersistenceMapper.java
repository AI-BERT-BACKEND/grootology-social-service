package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.ReferralPoints;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.ReferralPointsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReferralPointsPersistenceMapper {
    ReferralPoints toDomain(ReferralPointsEntity entity);
    ReferralPointsEntity toEntity(ReferralPoints points);
}
