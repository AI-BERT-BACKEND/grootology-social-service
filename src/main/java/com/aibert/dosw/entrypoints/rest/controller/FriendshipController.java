package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendshipResponseDTO>> listFriends(
            @PathVariable UUID userId,
            @RequestParam(required = false) OnlineStatus status) {
        return ResponseEntity.ok(friendshipUseCase.listFriends(userId, status));
    }

    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID userId,
            @PathVariable UUID friendId) {
        friendshipUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
