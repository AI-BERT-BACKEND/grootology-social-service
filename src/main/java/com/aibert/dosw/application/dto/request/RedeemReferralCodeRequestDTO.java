package com.aibert.dosw.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RedeemReferralCodeRequestDTO {
    @NotNull(message = "El ID del nuevo usuario es obligatorio")
    private UUID newUserId;
}
