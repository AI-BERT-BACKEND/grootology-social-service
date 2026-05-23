package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.ports.in.SocialPanelUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Social Panel", description = "Manage the central social hub: presence status, available actions and chat history. (R06)")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class SocialPanelController {

    private final SocialPanelUseCase socialPanelUseCase;

    @Operation(
        summary = "Get social panel",
        description = "Returns the user's social panel. Without an action it returns a full summary. With a specific action (INVITE, SEARCH, LIST, CHAT) it returns only the data relevant to that section.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Social panel retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{userId}/panel")
    public ResponseEntity<SocialPanelResponseDTO> getPanel(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @RequestParam(required = false) SocialAction action) {
        return ResponseEntity.ok(socialPanelUseCase.getPanel(userId, action));
    }
}
