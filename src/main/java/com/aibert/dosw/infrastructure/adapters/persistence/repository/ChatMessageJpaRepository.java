package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, UUID> {

    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "((m.senderId = :uid1 AND m.receiverId = :uid2 AND m.deletedBySender = false) OR " +
           "(m.senderId = :uid2 AND m.receiverId = :uid1 AND m.deletedByReceiver = false)) " +
           "ORDER BY m.sentAt DESC")
    List<ChatMessageEntity> findConversation(@Param("uid1") UUID uid1,
                                             @Param("uid2") UUID uid2,
                                             Pageable pageable);

    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
           "(m.senderId = :userId AND m.deletedBySender = false) OR " +
           "(m.receiverId = :userId AND m.deletedByReceiver = false) " +
           "ORDER BY m.sentAt DESC")
    List<ChatMessageEntity> findAllMessagesForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE " +
           "m.receiverId = :receiverId AND m.senderId = :senderId " +
           "AND m.readAt IS NULL AND m.deletedByReceiver = false")
    long countUnread(@Param("receiverId") UUID receiverId, @Param("senderId") UUID senderId);
}
