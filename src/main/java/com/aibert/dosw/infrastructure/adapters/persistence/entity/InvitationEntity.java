package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID inviterId;

    @Column(nullable = false, unique = true)
    private String referralCode;

    private String inviteeEmail;

    @Column(nullable = false)
    private boolean used;
}
