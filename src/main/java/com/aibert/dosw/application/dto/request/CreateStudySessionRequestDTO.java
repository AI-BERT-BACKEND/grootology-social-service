package com.aibert.dosw.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class CreateStudySessionRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String topic;

    @NotNull
    @Future
    private LocalDateTime scheduledAt;

    @NotNull
    @DecimalMin("0.5") @DecimalMax("8.0")
    private Double durationHours;

    @NotNull
    @Size(min = 1, max = 19)
    private List<UUID> participantIds;

    @Size(max = 500)
    private String notes;
}
