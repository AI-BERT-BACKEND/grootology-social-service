package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RedeemReferralCodeRequestDTO {
    @NotNull(message = "El ID del nuevo usuario es obligatorio")
    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID newUserId;
}
