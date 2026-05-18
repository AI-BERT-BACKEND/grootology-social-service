package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Sesiones de estudio", description = "Creación, respuesta y listado de sesiones de estudio colaborativas")
@RestController
@RequestMapping("/api/social/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(summary = "Crear sesión de estudio", description = "Crea una nueva sesión de estudio con participantes, tema, fecha y duración. El creador queda incluido automáticamente.")
    @PostMapping("/{creatorId}")
    public ResponseEntity<StudySessionResponseDTO> createSession(
            @PathVariable UUID creatorId,
            @Valid @RequestBody CreateStudySessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionUseCase.createSession(creatorId, request));
    }

    @Operation(summary = "Responder invitación a sesión", description = "Acepta o rechaza una invitación a sesión de estudio. El parámetro `accept=true` acepta; `accept=false` rechaza.")
    @PutMapping("/{sessionId}/respond")
    public ResponseEntity<StudySessionResponseDTO> respond(
            @PathVariable UUID sessionId,
            @RequestParam UUID userId,
            @RequestParam boolean accept) {
        return ResponseEntity.ok(studySessionUseCase.respondToSession(sessionId, userId, accept));
    }

    @Operation(summary = "Listar sesiones del usuario", description = "Devuelve todas las sesiones de estudio en las que el usuario es creador o participante.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudySessionResponseDTO>> getSessionsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getSessionsForUser(userId));
    }
}
