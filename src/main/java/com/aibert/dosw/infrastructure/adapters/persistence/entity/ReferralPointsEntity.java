package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "referral_points")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralPointsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private int totalPoints;

    @Column(nullable = false)
    private int weeklyPoints;

    @Column(nullable = false)
    private LocalDate weekStart;
}
