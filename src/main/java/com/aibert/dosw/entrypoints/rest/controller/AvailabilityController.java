package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Disponibilidad", description = "Configuración de visibilidad del perfil (PUBLIC / PRIVATE / SPECIFIC)")
@RestController
@RequestMapping("/api/social/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityUseCase availabilityUseCase;

    @Operation(summary = "Guardar configuración de visibilidad", description = "Establece cómo otros usuarios pueden encontrar al usuario en búsquedas: PUBLIC (todos), PRIVATE (solo amigos), SPECIFIC (lista autorizada).")
    @PutMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> saveConfig(
            @PathVariable UUID userId,
            @Valid @RequestBody AvailabilityConfigRequestDTO request) {
        return ResponseEntity.ok(availabilityUseCase.saveConfig(userId, request));
    }

    @Operation(summary = "Consultar configuración de visibilidad", description = "Devuelve la configuración de visibilidad actual del usuario. Si no tiene configuración, retorna PRIVATE por defecto.")
    @GetMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> getConfig(@PathVariable UUID userId) {
        return ResponseEntity.ok(availabilityUseCase.getConfig(userId));
    }
}
