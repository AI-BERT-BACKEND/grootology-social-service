package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_presence")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserPresenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime lastSeen;
}
