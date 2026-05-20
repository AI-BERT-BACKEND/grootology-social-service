package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.SendMessageRequestDTO;
import com.aibert.dosw.application.dto.response.ChatMessageResponseDTO;
import com.aibert.dosw.application.dto.response.ConversationSummaryResponseDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.ChatMessage;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.ChatUseCase;
import com.aibert.dosw.domain.ports.out.ChatMessageRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.NotificationPublisherPort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final ChatMessageRepositoryPort messageRepository;
    private final FriendshipRepositoryPort friendshipRepository;
    private final UserPresenceRepositoryPort userPresenceRepository;
    private final NotificationPublisherPort notificationPublisher;

    @Override
    public ChatMessageResponseDTO sendMessage(UUID senderId, SendMessageRequestDTO request) {
        UUID receiverId = request.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new ConnectionRequestException("No puedes enviarte un mensaje a ti mismo");
        }
        if (!friendshipRepository.existsByUserIds(senderId, receiverId)) {
            throw new ConnectionRequestException("Solo puedes enviar mensajes a tus amigos");
        }

        ChatMessage saved = messageRepository.save(ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .deletedBySender(false)
                .deletedByReceiver(false)
                .build());

        try {
            notificationPublisher.publish(NotificationEvent.builder()
                    .userId(receiverId)
                    .type("NEW_CHAT_MESSAGE")
                    .title("Nuevo mensaje")
                    .message("Tienes un nuevo mensaje")
                    .severity("INFO")
                    .relatedEntityId(saved.getId())
                    .build());
        } catch (Exception e) {
            log.error("Error publicando notificación de chat para receiverId={}: {}", receiverId, e.getMessage());
        }

        return toDTO(saved);
    }

    @Override
    public List<ChatMessageResponseDTO> getConversation(UUID userId, UUID friendId, int page, int size) {
        return messageRepository.findConversation(userId, friendId, page, size)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ConversationSummaryResponseDTO> getConversations(UUID userId) {
        List<ChatMessage> allMessages = messageRepository.findAllMessagesForUser(userId);

        Map<UUID, ChatMessage> lastMessageByContact = new java.util.LinkedHashMap<>();
        for (ChatMessage msg : allMessages) {
            UUID contactId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            lastMessageByContact.putIfAbsent(contactId, msg);
        }

        List<ConversationSummaryResponseDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, ChatMessage> entry : lastMessageByContact.entrySet()) {
            UUID contactId = entry.getKey();
            ChatMessage last = entry.getValue();
            UserPresence presence = userPresenceRepository.findByUserId(contactId).orElse(null);

            result.add(ConversationSummaryResponseDTO.builder()
                    .friendId(contactId)
                    .friendName(presence != null ? presence.getName() : null)
                    .friendAvatarUrl(presence != null ? presence.getAvatarUrl() : null)
                    .presenceStatus(UserPresenceResponseDTO.builder()
                            .userId(contactId)
                            .status(presence != null ? presence.getStatus() : OnlineStatus.OFFLINE)
                            .lastSeen(presence != null ? presence.getLastSeen() : null)
                            .avatarUrl(presence != null ? presence.getAvatarUrl() : null)
                            .build())
                    .lastMessageContent(last.getContent())
                    .lastMessageSenderId(last.getSenderId())
                    .lastMessageAt(last.getSentAt())
                    .unreadCount(messageRepository.countUnread(userId, contactId))
                    .build());
        }
        return result;
    }

    @Override
    public ChatMessageResponseDTO markAsRead(UUID messageId, UUID readerId) {
        ChatMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ConnectionRequestException("Mensaje no encontrado"));

        if (!msg.getReceiverId().equals(readerId)) {
            throw new ConnectionRequestException("No tienes permiso para marcar este mensaje como leído");
        }
        if (msg.getReadAt() != null) {
            return toDTO(msg);
        }

        return toDTO(messageRepository.save(msg.toBuilder()
                .readAt(LocalDateTime.now())
                .build()));
    }

    @Override
    public void deleteMessage(UUID messageId, UUID userId) {
        ChatMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ConnectionRequestException("Mensaje no encontrado"));

        boolean isSender = msg.getSenderId().equals(userId);
        boolean isReceiver = msg.getReceiverId().equals(userId);

        if (!isSender && !isReceiver) {
            throw new ConnectionRequestException("No tienes permiso para eliminar este mensaje");
        }

        messageRepository.save(msg.toBuilder()
                .deletedBySender(isSender || msg.isDeletedBySender())
                .deletedByReceiver(isReceiver || msg.isDeletedByReceiver())
                .build());
    }

    private ChatMessageResponseDTO toDTO(ChatMessage m) {
        return ChatMessageResponseDTO.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .receiverId(m.getReceiverId())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .read(m.getReadAt() != null)
                .build();
    }
}
