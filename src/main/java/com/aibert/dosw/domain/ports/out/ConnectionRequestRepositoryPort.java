package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.ConnectionRequest;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRequestRepositoryPort {
    ConnectionRequest save(ConnectionRequest request);
    Optional<ConnectionRequest> findById(UUID id);
    List<ConnectionRequest> findByReceiverIdAndStatus(UUID receiverId, ConnectionRequestStatus status);
    List<ConnectionRequest> findBySenderId(UUID senderId);
    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, ConnectionRequestStatus status);
    boolean existsByEitherDirectionAndStatus(UUID userId1, UUID userId2, ConnectionRequestStatus status);
}
