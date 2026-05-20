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

@Tag(name = "Study Sessions", description = "Create, respond to and list collaborative study sessions")
@RestController
@RequestMapping("/api/social/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionUseCase studySessionUseCase;

    @Operation(summary = "Create study session", description = "Creates a new study session with participants, topic, date and duration. The creator is automatically included as a participant.")
    @PostMapping("/{creatorId}")
    public ResponseEntity<StudySessionResponseDTO> createSession(
            @PathVariable UUID creatorId,
            @Valid @RequestBody CreateStudySessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionUseCase.createSession(creatorId, request));
    }

    @Operation(summary = "Respond to session invite", description = "Accepts or rejects a study session invitation. Use `accept=true` to accept and `accept=false` to reject.")
    @PutMapping("/{sessionId}/respond")
    public ResponseEntity<StudySessionResponseDTO> respond(
            @PathVariable UUID sessionId,
            @RequestParam UUID userId,
            @RequestParam boolean accept) {
        return ResponseEntity.ok(studySessionUseCase.respondToSession(sessionId, userId, accept));
    }

    @Operation(summary = "List user sessions", description = "Returns all study sessions where the user is either the creator or a participant.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudySessionResponseDTO>> getSessionsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getSessionsForUser(userId));
    }
}
