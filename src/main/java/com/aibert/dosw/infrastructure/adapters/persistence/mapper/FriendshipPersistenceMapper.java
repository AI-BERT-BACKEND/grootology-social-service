package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.FriendshipEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FriendshipPersistenceMapper {
    Friendship toDomain(FriendshipEntity entity);
    FriendshipEntity toEntity(Friendship friendship);
}
