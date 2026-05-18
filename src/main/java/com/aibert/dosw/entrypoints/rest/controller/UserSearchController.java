package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.ports.in.UserSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchUseCase userSearchUseCase;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> search(
            @RequestParam String q,
            @RequestParam UUID requesterId) {
        return ResponseEntity.ok(userSearchUseCase.search(q, requesterId));
    }
}
