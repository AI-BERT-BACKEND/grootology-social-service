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

@Tag(name = "Amigos", description = "Listado de amigos con estado online/offline y eliminación de amistad")
@RestController
@RequestMapping("/api/social/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;

    @Operation(summary = "Listar amigos", description = "Devuelve la lista de amigos con su estado de presencia (nombre, avatar, ONLINE/OFFLINE). Acepta filtro opcional por estado de conexión.")
    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendshipResponseDTO>> listFriends(
            @PathVariable UUID userId,
            @RequestParam(required = false) OnlineStatus status) {
        return ResponseEntity.ok(friendshipUseCase.listFriends(userId, status));
    }

    @Operation(summary = "Eliminar amigo", description = "Elimina la amistad entre dos usuarios. La acción es irreversible; para reconectarse habría que enviar una nueva solicitud.")
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID userId,
            @PathVariable UUID friendId) {
        friendshipUseCase.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
