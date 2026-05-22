package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.model.user.SessionStatus;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudySessionControllerTest {

    private final StudySessionUseCase useCase = mock(StudySessionUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StudySessionController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    private StudySessionResponseDTO buildSession() {
        return StudySessionResponseDTO.builder()
                .id(UUID.randomUUID()).topic("Calculo")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationHours(2.0)
                .participantIds(List.of(UUID.randomUUID()))
                .status(SessionStatus.PENDING)
                .notes("Notas").build();
    }

    @Test
    void createSession_validInput_returns201() throws Exception {
        UUID creatorId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        when(useCase.createSession(eq(creatorId), any())).thenReturn(buildSession());

        mockMvc.perform(post("/api/social/sessions/{creatorId}", creatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "topic", "Calculo",
                                "scheduledAt", "2030-01-01T10:00:00",
                                "durationHours", 2.0,
                                "participantIds", List.of(participantId.toString())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.topic").value("Calculo"));
    }

    @Test
    void respond_validInput_returns200() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StudySessionResponseDTO dto = StudySessionResponseDTO.builder()
                .id(sessionId).topic("Calculo")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationHours(2.0)
                .participantIds(List.of(userId))
                .status(SessionStatus.ACCEPTED).build();
        when(useCase.respondToSession(eq(sessionId), eq(userId), anyBoolean())).thenReturn(dto);

        mockMvc.perform(put("/api/social/sessions/{sessionId}/respond", sessionId)
                        .param("userId", userId.toString())
                        .param("accept", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void getSessionsForUser_validUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(useCase.getSessionsForUser(userId)).thenReturn(List.of(buildSession()));

        mockMvc.perform(get("/api/social/sessions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topic").value("Calculo"));
    }
}
