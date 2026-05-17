package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId1;

    @Column(nullable = false)
    private UUID userId2;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
