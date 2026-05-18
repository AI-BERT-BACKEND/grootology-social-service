package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ChatUseCase {
    ChatMessageResponseDTO sendMessage(UUID senderId, SendMessageRequestDTO request);
    List<ChatMessageResponseDTO> getConversation(UUID userId, UUID friendId, int page, int size);
    List<ConversationSummaryResponseDTO> getConversations(UUID userId);
    ChatMessageResponseDTO markAsRead(UUID messageId, UUID readerId);
    void deleteMessage(UUID messageId, UUID userId);
}
