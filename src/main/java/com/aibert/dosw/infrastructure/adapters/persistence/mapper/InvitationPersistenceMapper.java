package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.InvitationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvitationPersistenceMapper {
    Invitation toDomain(InvitationEntity entity);
    InvitationEntity toEntity(Invitation invitation);
}
