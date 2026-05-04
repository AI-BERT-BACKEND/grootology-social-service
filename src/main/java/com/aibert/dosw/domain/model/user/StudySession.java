package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StudySession {
    private UUID id;
    private UUID creatorId;
    private String topic;
    private LocalDateTime scheduledAt;
    private double durationHours;
    private String notes;
    private List<UUID> participantIds;
    private SessionStatus status;
}
