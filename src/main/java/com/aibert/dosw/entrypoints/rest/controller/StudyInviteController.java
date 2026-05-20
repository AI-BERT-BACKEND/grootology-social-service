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

@Tag(name = "Study Session Invites", description = "Endpoint consumed by the notification service to retrieve pending study session invites for a user")
@RestController
@RequestMapping("/api/v1/social/user")
@RequiredArgsConstructor
public class StudyInviteController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(
            summary = "Get pending study invites",
            description = "Returns study sessions with PENDING status where the user is a participant but not the creator. Used by the notification service."
    )
    @GetMapping("/{userId}/study-invites/pending")
    public ResponseEntity<List<StudySessionResponseDTO>> getPendingInvites(@PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getPendingInvitesForUser(userId));
    }
}
