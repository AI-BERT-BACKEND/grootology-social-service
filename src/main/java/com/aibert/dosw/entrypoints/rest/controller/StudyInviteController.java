package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Study Session Invites", description = "Consulta de invitaciones a sesiones de estudio pendientes de un usuario")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class StudyInviteController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(
            summary = "Obtener invitaciones de estudio pendientes",
            description = "Devuelve las sesiones de estudio con status PENDING donde el usuario es participante (no creador). "
                    + "Ruta completa: GET /api/social/users/{userId}/study-invites/pending."
    )
    @GetMapping("/{userId}/study-invites/pending")
    public ResponseEntity<List<StudySessionResponseDTO>> getPendingInvites(@PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getPendingInvitesForUser(userId));
    }
}
