package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AvailabilityControllerTest {

    private final AvailabilityUseCase useCase = mock(AvailabilityUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvailabilityController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void saveConfig_validInput_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        AvailabilityConfig config = AvailabilityConfig.builder()
                .id(UUID.randomUUID()).userId(userId)
                .visibility(VisibilityLevel.PUBLIC).build();
        when(useCase.saveConfig(eq(userId), any())).thenReturn(config);

        mockMvc.perform(put("/api/social/availability/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("visibility", "PUBLIC"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void getConfig_validUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        AvailabilityConfig config = AvailabilityConfig.builder()
                .id(UUID.randomUUID()).userId(userId)
                .visibility(VisibilityLevel.PRIVATE).build();
        when(useCase.getConfig(userId)).thenReturn(config);

        mockMvc.perform(get("/api/social/availability/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }
}
