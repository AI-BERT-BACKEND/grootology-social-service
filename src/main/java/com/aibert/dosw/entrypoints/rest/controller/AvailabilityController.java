package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Availability", description = "Manage profile visibility settings: control who can find you in searches (PUBLIC / PRIVATE / SPECIFIC). (R03)")
@RestController
@RequestMapping("/api/social/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityUseCase availabilityUseCase;

    @Operation(
        summary = "Save visibility settings",
        description = "Sets how other users can find this user in searches: PUBLIC (everyone), PRIVATE (friends only), SPECIFIC (authorized list only).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Visibility settings saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid visibility level or request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to access this resource")
    @PutMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> saveConfig(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Valid @RequestBody AvailabilityConfigRequestDTO request,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Id",
                    description = "Authenticated user UUID. Use this in Swagger when testing directly without a gateway-issued JWT.",
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Email",
                    description = "Authenticated user email. Use this in Swagger when testing directly without a gateway-issued JWT.",
                    example = "user@test.com")
            @RequestHeader(value = "X-User-Email", required = false) String authenticatedUserEmail) {
        return ResponseEntity.ok(availabilityUseCase.saveConfig(userId, request));
    }

    @Operation(
        summary = "Get visibility settings",
        description = "Returns the current visibility configuration for the user. If no configuration exists, PRIVATE is returned by default.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Visibility settings retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to access this resource")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}")
    public ResponseEntity<AvailabilityConfig> getConfig(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Id",
                    description = "Authenticated user UUID. Use this in Swagger when testing directly without a gateway-issued JWT.",
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Email",
                    description = "Authenticated user email. Use this in Swagger when testing directly without a gateway-issued JWT.",
                    example = "user@test.com")
            @RequestHeader(value = "X-User-Email", required = false) String authenticatedUserEmail) {
        return ResponseEntity.ok(availabilityUseCase.getConfig(userId));
    }
}
