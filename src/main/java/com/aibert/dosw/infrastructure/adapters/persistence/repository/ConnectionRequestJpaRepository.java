package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.ConnectionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConnectionRequestJpaRepository extends JpaRepository<ConnectionRequestEntity, UUID> {
    List<ConnectionRequestEntity> findByReceiverIdAndStatus(UUID receiverId, ConnectionRequestStatus status);
    List<ConnectionRequestEntity> findBySenderId(UUID senderId);
    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, ConnectionRequestStatus status);
}
