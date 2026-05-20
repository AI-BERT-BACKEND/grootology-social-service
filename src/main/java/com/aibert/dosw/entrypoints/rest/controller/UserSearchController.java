package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.ports.in.UserSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Search", description = "Search users by name or email while respecting their privacy settings (PUBLIC / PRIVATE / SPECIFIC)")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchUseCase userSearchUseCase;

    @Operation(summary = "Search users", description = "Searches for users by name or institutional email. Respects privacy settings: PRIVATE users only appear to their friends, SPECIFIC users only to those on their authorized list. Each result includes the online status and current relationship with the requester.")
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> search(
            @RequestParam String q,
            @RequestParam UUID requesterId) {
        return ResponseEntity.ok(userSearchUseCase.search(q, requesterId));
    }
}
