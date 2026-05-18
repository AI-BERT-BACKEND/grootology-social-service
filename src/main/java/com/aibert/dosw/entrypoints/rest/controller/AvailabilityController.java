package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/social/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityUseCase availabilityUseCase;

    @PutMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> saveConfig(
            @PathVariable UUID userId,
            @Valid @RequestBody AvailabilityConfigRequestDTO request) {
        return ResponseEntity.ok(availabilityUseCase.saveConfig(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> getConfig(@PathVariable UUID userId) {
        return ResponseEntity.ok(availabilityUseCase.getConfig(userId));
    }
}
