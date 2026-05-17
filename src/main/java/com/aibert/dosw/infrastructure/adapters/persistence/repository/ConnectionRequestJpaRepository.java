package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.ConnectionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConnectionRequestJpaRepository extends JpaRepository<ConnectionRequestEntity, UUID> {
    List<ConnectionRequestEntity> findByReceiverIdAndStatus(UUID receiverId, ConnectionRequestStatus status);
    List<ConnectionRequestEntity> findBySenderId(UUID senderId);
    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, ConnectionRequestStatus status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM ConnectionRequestEntity r " +
           "WHERE ((r.senderId = :uid1 AND r.receiverId = :uid2) OR (r.senderId = :uid2 AND r.receiverId = :uid1)) " +
           "AND r.status = :status")
    boolean existsByEitherDirectionAndStatus(@Param("uid1") UUID uid1,
                                             @Param("uid2") UUID uid2,
                                             @Param("status") ConnectionRequestStatus status);
}
