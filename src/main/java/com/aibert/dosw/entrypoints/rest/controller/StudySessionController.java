package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateStudySessionRequestDTO;
import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionUseCase studySessionUseCase;

    @PostMapping("/{creatorId}")
    public ResponseEntity<StudySessionResponseDTO> createSession(
            @PathVariable UUID creatorId,
            @Valid @RequestBody CreateStudySessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionUseCase.createSession(creatorId, request));
    }

    @PutMapping("/{sessionId}/respond")
    public ResponseEntity<StudySessionResponseDTO> respond(
            @PathVariable UUID sessionId,
            @RequestParam UUID userId,
            @RequestParam boolean accept) {
        return ResponseEntity.ok(studySessionUseCase.respondToSession(sessionId, userId, accept));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudySessionResponseDTO>> getSessionsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(studySessionUseCase.getSessionsForUser(userId));
    }
}
