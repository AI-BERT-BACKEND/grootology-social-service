package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class CreateStudySessionRequestDTO {

    @NotBlank
    @Size(max = 150)
    @Schema(example = "Algebra Lineal — Vectores y matrices")
    private String topic;

    @NotNull
    @Future
    @Schema(example = "2026-05-22T16:52:39.763Z")
    private LocalDateTime scheduledAt;

    @NotNull
    @DecimalMin("0.5") @DecimalMax("8.0")
    @Schema(example = "1.5")
    private Double durationHours;

    @NotNull
    @Size(min = 1, max = 19)
    @Schema(example = "[\"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"b2c3d4e5-f6a7-8901-bcde-f12345678901\"]")
    private List<UUID> participantIds;

    @Size(max = 500)
    @Schema(example = "Traer calculadora y apuntes del capítulo 3")
    private String notes;
}
