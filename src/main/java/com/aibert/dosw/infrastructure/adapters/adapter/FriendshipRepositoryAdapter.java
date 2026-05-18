package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.FriendshipPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.FriendshipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendshipRepositoryAdapter implements FriendshipRepositoryPort {

    private final FriendshipJpaRepository jpaRepository;
    private final FriendshipPersistenceMapper mapper;

    @Override
    public Friendship save(Friendship friendship) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(friendship)));
    }

    @Override
    public List<Friendship> findByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIds(UUID userId1, UUID userId2) {
        return jpaRepository.existsByUserIds(userId1, userId2);
    }

    @Override
    public void deleteByUserIds(UUID userId1, UUID userId2) {
        jpaRepository.deleteByUserIds(userId1, userId2);
    }
}
