package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.ChatMessage;
import com.aibert.dosw.domain.ports.out.ChatMessageRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.ChatMessagePersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepositoryPort {

    private final ChatMessageJpaRepository jpaRepository;
    private final ChatMessagePersistenceMapper mapper;

    @Override
    public ChatMessage save(ChatMessage message) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(message)));
    }

    @Override
    public Optional<ChatMessage> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ChatMessage> findConversation(UUID userId1, UUID userId2, int page, int size) {
        return jpaRepository.findConversation(userId1, userId2, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findAllMessagesForUser(UUID userId) {
        return jpaRepository.findAllMessagesForUser(userId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countUnread(UUID receiverId, UUID senderId) {
        return jpaRepository.countUnread(receiverId, senderId);
    }
}
