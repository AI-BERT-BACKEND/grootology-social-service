package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.ports.in.SocialPanelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class SocialPanelController {

    private final SocialPanelUseCase socialPanelUseCase;

    @GetMapping("/{userId}/panel")
    public ResponseEntity<SocialPanelResponseDTO> getPanel(
            @PathVariable UUID userId,
            @RequestParam(required = false) SocialAction action) {
        return ResponseEntity.ok(socialPanelUseCase.getPanel(userId, action));
    }
}
