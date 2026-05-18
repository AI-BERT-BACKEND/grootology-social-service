package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connection_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConnectionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
