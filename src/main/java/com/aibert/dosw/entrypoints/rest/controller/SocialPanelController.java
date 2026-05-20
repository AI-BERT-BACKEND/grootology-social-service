package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.ports.in.SocialPanelUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Social Panel", description = "Central hub of the social module: presence status, available actions and chat history")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class SocialPanelController {

    private final SocialPanelUseCase socialPanelUseCase;

    @Operation(
        summary = "Get social panel",
        description = "Returns the user's social panel. Without an action it returns a full summary. With a specific action (INVITE, SEARCH, LIST, CHAT) it returns only the data relevant to that section."
    )
    @GetMapping("/{userId}/panel")
    public ResponseEntity<SocialPanelResponseDTO> getPanel(
            @PathVariable UUID userId,
            @RequestParam(required = false) SocialAction action) {
        return ResponseEntity.ok(socialPanelUseCase.getPanel(userId, action));
    }
}
