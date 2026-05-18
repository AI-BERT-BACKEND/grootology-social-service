package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.FriendshipEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, UUID> {

    @Query("SELECT f FROM FriendshipEntity f WHERE f.userId1 = :userId OR f.userId2 = :userId")
    List<FriendshipEntity> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM FriendshipEntity f " +
           "WHERE (f.userId1 = :uid1 AND f.userId2 = :uid2) OR (f.userId1 = :uid2 AND f.userId2 = :uid1)")
    boolean existsByUserIds(@Param("uid1") UUID uid1, @Param("uid2") UUID uid2);

    @Modifying
    @Transactional
    @Query("DELETE FROM FriendshipEntity f " +
           "WHERE (f.userId1 = :uid1 AND f.userId2 = :uid2) OR (f.userId1 = :uid2 AND f.userId2 = :uid1)")
    void deleteByUserIds(@Param("uid1") UUID uid1, @Param("uid2") UUID uid2);
}
