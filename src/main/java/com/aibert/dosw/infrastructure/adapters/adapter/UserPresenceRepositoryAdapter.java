package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.UserPresencePersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.UserPresenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPresenceRepositoryAdapter implements UserPresenceRepositoryPort {

    private final UserPresenceJpaRepository jpaRepository;
    private final UserPresencePersistenceMapper mapper;

    @Override
    public UserPresence save(UserPresence presence) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(presence)));
    }

    @Override
    public Optional<UserPresence> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
