package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.InvitationPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.InvitationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvitationRepositoryAdapter implements InvitationRepositoryPort {
    private final InvitationJpaRepository jpaRepository;
    private final InvitationPersistenceMapper mapper;

    @Override
    public Invitation save(Invitation invitation) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(invitation)));
    }

    @Override
    public Optional<Invitation> findByReferralCode(String code) {
        return jpaRepository.findByReferralCode(code).map(mapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByInviterId(UUID inviterId) {
        return jpaRepository.findByInviterId(inviterId).map(mapper::toDomain);
    }
}
