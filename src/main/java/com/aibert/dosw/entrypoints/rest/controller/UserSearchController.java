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

@Tag(name = "Búsqueda de usuarios", description = "Búsqueda por nombre o correo respetando privacidad RN-05 (PUBLIC / PRIVATE / SPECIFIC)")
@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchUseCase userSearchUseCase;

    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre o correo institucional. Aplica privacidad RN-05: usuarios PRIVATE solo aparecen para sus amigos; usuarios SPECIFIC solo para quienes están en su lista autorizada. Cada resultado incluye estado online y relación actual.")
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> search(
            @RequestParam String q,
            @RequestParam UUID requesterId) {
        return ResponseEntity.ok(userSearchUseCase.search(q, requesterId));
    }
}
