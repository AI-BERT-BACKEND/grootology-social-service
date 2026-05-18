package com.aibert.dosw.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendMessageRequestDTO {
    @NotNull(message = "El destinatario es obligatorio")
    private UUID receiverId;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 2000, message = "El mensaje no puede superar los 2000 caracteres")
    private String content;
}
