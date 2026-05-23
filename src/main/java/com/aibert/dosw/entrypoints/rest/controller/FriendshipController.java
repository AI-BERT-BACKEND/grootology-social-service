package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friends", description = "Manage the friends list: retrieve with presence status and remove friendships. (R02, R05)")
@RestController
@RequestMapping("/api/social/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @Operation(
        summary = "List friends",
        description = "Returns the list of friends with their presence status (name, avatar, ONLINE/OFFLINE). Accepts an optional filter by connection status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Friends list retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendshipResponseDTO>> listFriends(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @RequestParam(required = false) OnlineStatus status) {
        return ResponseEntity.ok(friendshipUseCase.listFriends(userId, status));
    }

    @Operation(
        summary = "Remove friend",
        description = "Removes the friendship between two users. This action is irreversible. To reconnect, a new friend request must be sent.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Friendship removed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Friendship not found")
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @Parameter(description = "UUID of the user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID userId,
            @Parameter(description = "UUID of the friend to remove", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901", schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID friendId) {
        friendshipUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
