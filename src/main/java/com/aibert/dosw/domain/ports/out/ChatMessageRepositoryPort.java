package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.ChatMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepositoryPort {
    ChatMessage save(ChatMessage message);
    Optional<ChatMessage> findById(UUID id);
    List<ChatMessage> findConversation(UUID userId1, UUID userId2, int page, int size);
    List<ChatMessage> findAllMessagesForUser(UUID userId);
    long countUnread(UUID receiverId, UUID senderId);
}
