package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import com.aibert.dosw.domain.model.user.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "study_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudySessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private double durationHours;

    private String notes;

    @ElementCollection
    @CollectionTable(name = "session_participants", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "participant_id")
    private List<UUID> participantIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
}
