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

@Tag(name = "Availability", description = "Profile visibility settings: control who can find you in searches (PUBLIC / PRIVATE / SPECIFIC)")
@RestController
@RequestMapping("/api/social/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityUseCase availabilityUseCase;

    @Operation(summary = "Save visibility settings", description = "Sets how other users can find this user in searches: PUBLIC (everyone), PRIVATE (friends only), SPECIFIC (authorized list only).")
    @PutMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> saveConfig(
            @PathVariable UUID userId,
            @Valid @RequestBody AvailabilityConfigRequestDTO request) {
        return ResponseEntity.ok(availabilityUseCase.saveConfig(userId, request));
    }

    @Operation(summary = "Get visibility settings", description = "Returns the current visibility configuration for the user. If no configuration exists, PRIVATE is returned by default.")
    @GetMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> getConfig(@PathVariable UUID userId) {
        return ResponseEntity.ok(availabilityUseCase.getConfig(userId));
    }
}
