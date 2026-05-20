package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.user.ChatMessage;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.ChatMessageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessagePersistenceMapper {
    ChatMessage toDomain(ChatMessageEntity entity);
    ChatMessageEntity toEntity(ChatMessage message);
}
