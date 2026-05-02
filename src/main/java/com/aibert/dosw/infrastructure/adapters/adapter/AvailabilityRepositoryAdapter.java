package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.ports.out.AvailabilityRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.AvailabilityPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.AvailabilityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvailabilityRepositoryAdapter implements AvailabilityRepositoryPort {
    private final AvailabilityJpaRepository jpaRepository;
    private final AvailabilityPersistenceMapper mapper;

    @Override
    public AvailabilityConfig save(AvailabilityConfig config) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(config)));
    }

    @Override
    public Optional<AvailabilityConfig> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }
}
