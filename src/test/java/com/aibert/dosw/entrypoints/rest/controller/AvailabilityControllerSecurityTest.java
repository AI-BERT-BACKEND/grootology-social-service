package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.config.JwtAuthFilter;
import com.aibert.dosw.config.SecurityConfig;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.in.AvailabilityUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:1509",
        "jwt.secret=12345678901234567890123456789012"
})
class AvailabilityControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailabilityUseCase availabilityUseCase;

    @Test
    void getConfig_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/social/availability/{userId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getConfig_withGatewayUserIdHeader_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(availabilityUseCase.getConfig(userId)).thenReturn(AvailabilityConfig.builder()
                .userId(userId)
                .visibility(VisibilityLevel.PUBLIC)
                .build());

        mockMvc.perform(get("/api/social/availability/{userId}", userId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }
}
