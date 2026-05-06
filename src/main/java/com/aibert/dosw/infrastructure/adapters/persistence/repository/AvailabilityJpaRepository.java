package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.AvailabilityConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityJpaRepository extends JpaRepository<AvailabilityConfigEntity, UUID> {
    Optional<AvailabilityConfigEntity> findByUserId(UUID userId);
}
