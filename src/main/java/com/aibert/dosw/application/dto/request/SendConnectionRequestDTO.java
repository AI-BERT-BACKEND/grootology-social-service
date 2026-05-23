package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendConnectionRequestDTO {
    @NotNull(message = "El ID del destinatario es obligatorio")
    @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private UUID receiverId;
}
