package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StudySessionResponseDTO {
    private UUID id;
    private String topic;
    private LocalDateTime scheduledAt;
    private double durationHours;
    private List<UUID> participantIds;
    private SessionStatus status;
    private String notes;
}
