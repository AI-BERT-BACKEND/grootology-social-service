package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.ReferralPointsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReferralPointsJpaRepository extends JpaRepository<ReferralPointsEntity, UUID> {
    Optional<ReferralPointsEntity> findByUserId(UUID userId);
}
