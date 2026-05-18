package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.Friendship;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepositoryPort {
    Friendship save(Friendship friendship);
    List<Friendship> findByUserId(UUID userId);
    boolean existsByUserIds(UUID userId1, UUID userId2);
    void deleteByUserIds(UUID userId1, UUID userId2);
}
