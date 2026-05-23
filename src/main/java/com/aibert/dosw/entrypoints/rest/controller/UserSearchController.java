package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.ports.in.UserSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "User Search", description = "Search users by name or email while respecting their privacy settings (PUBLIC / PRIVATE / SPECIFIC)")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchUseCase userSearchUseCase;

    @Operation(
        summary = "Search users",
        description = "Searches for users by name or institutional email. Respects privacy settings: PRIVATE users only appear to their friends, SPECIFIC users only to those on their authorized list. Each result includes the online status and current relationship with the requester.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> search(
            @RequestParam String q,
            @RequestParam UUID requesterId) {
        log.info("GET /users/search - q='{}' requesterId={}", q, requesterId);
        return ResponseEntity.ok(userSearchUseCase.search(q, requesterId));
    }
}
