package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.UserPresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPresenceJpaRepository extends JpaRepository<UserPresenceEntity, UUID> {
    Optional<UserPresenceEntity> findByUserId(UUID userId);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserPresenceEntity u WHERE u.userId <> :excludeUserId " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UserPresenceEntity> searchByNameOrEmail(@Param("query") String query,
                                                 @Param("excludeUserId") UUID excludeUserId);
}
