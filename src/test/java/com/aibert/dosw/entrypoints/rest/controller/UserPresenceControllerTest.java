package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.ports.in.UserPresenceUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserPresenceControllerTest {

    private final UserPresenceUseCase useCase = mock(UserPresenceUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new UserPresenceController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void heartbeat_retorna200ConEstado() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPresenceResponseDTO dto = UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.ONLINE).lastSeen(LocalDateTime.now()).build();

        when(useCase.heartbeat(eq(userId), any(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/social/users/{userId}/heartbeat", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "user@test.com", "name", "Juan"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }

    @Test
    void getStatus_retorna200ConEstado() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPresenceResponseDTO dto = UserPresenceResponseDTO.builder()
                .userId(userId).status(OnlineStatus.OFFLINE).build();

        when(useCase.getStatus(userId)).thenReturn(dto);

        mockMvc.perform(get("/api/social/users/{userId}/status", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OFFLINE"));
    }
}
