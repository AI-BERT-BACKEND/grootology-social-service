package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.FriendshipResponseDTO;
import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.ports.in.FriendshipUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FriendshipControllerTest {

    private final FriendshipUseCase useCase = mock(FriendshipUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new FriendshipController(useCase)).build();

    private FriendshipResponseDTO buildDTO(UUID friendId, OnlineStatus status) {
        return FriendshipResponseDTO.builder()
                .id(UUID.randomUUID()).friendId(friendId)
                .friendName("Amigo").friendEmail("amigo@test.com")
                .presenceStatus(UserPresenceResponseDTO.builder()
                        .userId(friendId).status(status).lastSeen(LocalDateTime.now()).build())
                .since(LocalDateTime.now().minusDays(5))
                .build();
    }

    @Test
    void listFriends_sinFiltro_retornaTodasLasAmistades() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        when(useCase.listFriends(userId, null)).thenReturn(List.of(buildDTO(friendId, OnlineStatus.ONLINE)));

        mockMvc.perform(get("/api/social/friends/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].friendId").value(friendId.toString()))
                .andExpect(jsonPath("$[0].presenceStatus.status").value("ONLINE"));
    }

    @Test
    void listFriends_conFiltroOnline_retornaSoloOnline() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        when(useCase.listFriends(userId, OnlineStatus.ONLINE))
                .thenReturn(List.of(buildDTO(friendId, OnlineStatus.ONLINE)));

        mockMvc.perform(get("/api/social/friends/{userId}", userId)
                        .param("status", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].presenceStatus.status").value("ONLINE"));
    }
}
