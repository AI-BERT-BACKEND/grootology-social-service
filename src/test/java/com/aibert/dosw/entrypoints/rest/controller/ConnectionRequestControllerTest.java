package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConnectionRequestControllerTest {

    private final ConnectionRequestUseCase useCase = mock(ConnectionRequestUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new ConnectionRequestController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    private ConnectionRequestResponseDTO buildDTO(UUID senderId, UUID receiverId) {
        return ConnectionRequestResponseDTO.builder()
                .id(UUID.randomUUID()).senderId(senderId).receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING).sentAt(LocalDateTime.now()).build();
    }

    @Test
    void sendRequest_validInput_returns201() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        when(useCase.sendRequest(eq(senderId), any())).thenReturn(buildDTO(senderId, receiverId));

        mockMvc.perform(post("/api/social/connections/{senderId}/request", senderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("receiverId", receiverId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void acceptRequest_validInput_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        ConnectionRequestResponseDTO dto = ConnectionRequestResponseDTO.builder()
                .id(requestId).senderId(UUID.randomUUID()).receiverId(receiverId)
                .status(ConnectionRequestStatus.ACCEPTED).sentAt(LocalDateTime.now()).build();

        when(useCase.acceptRequest(requestId, receiverId)).thenReturn(dto);

        mockMvc.perform(put("/api/social/connections/{requestId}/accept", requestId)
                        .param("receiverId", receiverId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void rejectRequest_validInput_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        ConnectionRequestResponseDTO dto = ConnectionRequestResponseDTO.builder()
                .id(requestId).senderId(UUID.randomUUID()).receiverId(receiverId)
                .status(ConnectionRequestStatus.REJECTED).sentAt(LocalDateTime.now()).build();

        when(useCase.rejectRequest(requestId, receiverId)).thenReturn(dto);

        mockMvc.perform(put("/api/social/connections/{requestId}/reject", requestId)
                        .param("receiverId", receiverId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void getPendingRequests_returns200() throws Exception {
        UUID receiverId = UUID.randomUUID();
        when(useCase.getPendingRequestsForUser(receiverId))
                .thenReturn(List.of(buildDTO(UUID.randomUUID(), receiverId)));

        mockMvc.perform(get("/api/social/connections/pending/{receiverId}", receiverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getSentRequests_returns200() throws Exception {
        UUID senderId = UUID.randomUUID();
        when(useCase.getSentRequestsByUser(senderId))
                .thenReturn(List.of(buildDTO(senderId, UUID.randomUUID())));

        mockMvc.perform(get("/api/social/connections/sent/{senderId}", senderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value(senderId.toString()));
    }
}
