package com.aibert.dosw.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendConnectionRequestDTO {
    @NotNull(message = "El ID del destinatario es obligatorio")
    private UUID receiverId;
}
