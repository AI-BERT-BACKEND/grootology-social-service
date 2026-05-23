package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class HeartbeatRequestDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Schema(example = "ana.garcia@universidad.edu")
    private String email;

    @Schema(example = "Ana García")
    private String name;

    @Schema(example = "https://cdn.aibert.app/avatars/ana-garcia.png")
    private String avatarUrl;
}
