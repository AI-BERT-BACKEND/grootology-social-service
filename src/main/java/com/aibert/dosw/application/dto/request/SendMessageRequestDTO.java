package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendMessageRequestDTO {
    @NotNull(message = "El destinatario es obligatorio")
    @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private UUID receiverId;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 2000, message = "El mensaje no puede superar los 2000 caracteres")
    @Schema(example = "¿A qué hora nos conectamos para repasar el capítulo 4?")
    private String content;
}
