package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.UserPresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPresenceJpaRepository extends JpaRepository<UserPresenceEntity, UUID> {
    Optional<UserPresenceEntity> findByUserId(UUID userId);
    boolean existsByEmail(String email);
}
