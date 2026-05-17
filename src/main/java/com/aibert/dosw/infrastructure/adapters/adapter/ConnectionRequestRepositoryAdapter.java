package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.user.ConnectionRequest;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.ports.out.ConnectionRequestRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.ConnectionRequestPersistenceMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.ConnectionRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConnectionRequestRepositoryAdapter implements ConnectionRequestRepositoryPort {

    private final ConnectionRequestJpaRepository jpaRepository;
    private final ConnectionRequestPersistenceMapper mapper;

    @Override
    public ConnectionRequest save(ConnectionRequest request) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(request)));
    }

    @Override
    public Optional<ConnectionRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ConnectionRequest> findByReceiverIdAndStatus(UUID receiverId, ConnectionRequestStatus status) {
        return jpaRepository.findByReceiverIdAndStatus(receiverId, status)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ConnectionRequest> findBySenderId(UUID senderId) {
        return jpaRepository.findBySenderId(senderId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, ConnectionRequestStatus status) {
        return jpaRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, status);
    }

    @Override
    public boolean existsByEitherDirectionAndStatus(UUID userId1, UUID userId2, ConnectionRequestStatus status) {
        return jpaRepository.existsByEitherDirectionAndStatus(userId1, userId2, status);
    }
}
