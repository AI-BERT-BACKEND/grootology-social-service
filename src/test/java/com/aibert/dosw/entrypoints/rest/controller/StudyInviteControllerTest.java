package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.StudySessionResponseDTO;
import com.aibert.dosw.domain.model.user.SessionStatus;
import com.aibert.dosw.domain.ports.in.StudySessionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudyInviteControllerTest {

    private final StudySessionUseCase useCase = mock(StudySessionUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StudyInviteController(useCase)).build();

    @Test
    void getPendingInvites_validUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        StudySessionResponseDTO dto = StudySessionResponseDTO.builder()
                .id(UUID.randomUUID()).topic("Calculo")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationHours(1.5)
                .participantIds(List.of(userId))
                .status(SessionStatus.PENDING).build();
        when(useCase.getPendingInvitesForUser(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/social/users/{userId}/study-invites/pending", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}
