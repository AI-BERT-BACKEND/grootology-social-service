package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.ReferralPoints;
import com.aibert.dosw.domain.ports.out.ReferralPointsRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.ReferralPointsPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.ReferralPointsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReferralPointsRepositoryAdapter implements ReferralPointsRepositoryPort {

    private final ReferralPointsJpaRepository jpaRepository;
    private final ReferralPointsPersistenceMapper mapper;

    @Override
    public ReferralPoints save(ReferralPoints points) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(points)));
    }

    @Override
    public Optional<ReferralPoints> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }
}
