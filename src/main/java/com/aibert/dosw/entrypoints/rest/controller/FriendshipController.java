package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friends", description = "List friends with their online/offline status and remove friendships")
@RestController
@RequestMapping("/api/social/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @Operation(summary = "List friends", description = "Returns the list of friends with their presence status (name, avatar, ONLINE/OFFLINE). Accepts an optional filter by connection status.")
    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendshipResponseDTO>> listFriends(
            @PathVariable UUID userId,
            @RequestParam(required = false) OnlineStatus status) {
        return ResponseEntity.ok(friendshipUseCase.listFriends(userId, status));
    }

    @Operation(summary = "Remove friend", description = "Removes the friendship between two users. This action is irreversible. To reconnect, a new friend request must be sent.")
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID userId,
            @PathVariable UUID friendId) {
        friendshipUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
