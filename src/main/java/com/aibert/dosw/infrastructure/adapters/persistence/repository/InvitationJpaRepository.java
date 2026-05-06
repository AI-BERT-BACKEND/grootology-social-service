package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InvitationJpaRepository extends JpaRepository<InvitationEntity, UUID> {
    Optional<InvitationEntity> findByReferralCode(String code);
    Optional<InvitationEntity> findByInviterId(UUID inviterId);
}
