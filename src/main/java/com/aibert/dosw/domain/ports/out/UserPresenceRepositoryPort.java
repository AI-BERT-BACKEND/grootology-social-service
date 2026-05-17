package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.UserPresence;

import java.util.Optional;
import java.util.UUID;

public interface UserPresenceRepositoryPort {
    UserPresence save(UserPresence presence);
    Optional<UserPresence> findByUserId(UUID userId);
    boolean existsByEmail(String email);
}
