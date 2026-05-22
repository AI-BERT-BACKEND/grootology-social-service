package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InvitationControllerTest {

    private final InvitationUseCase useCase = mock(InvitationUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new InvitationController(useCase)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void getReferralLink_validUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(useCase.getOrCreateReferralLink(userId)).thenReturn(
                InviteResponseDTO.builder()
                        .referralLink("https://aibert.app/r/ABC123")
                        .message("Enlace generado").build());

        mockMvc.perform(get("/api/social/invitations/{userId}/link", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referralLink").value("https://aibert.app/r/ABC123"));
    }

    @Test
    void sendInvitations_validInput_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(useCase).sendInvitations(eq(userId), any());

        mockMvc.perform(post("/api/social/invitations/{userId}/send", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                Map.of("emails", List.of("amigo@mail.com")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitations sent successfully."));

        verify(useCase).sendInvitations(eq(userId), any());
    }

    @Test
    void redeemCode_validInput_returns200() throws Exception {
        UUID newUserId = UUID.randomUUID();
        when(useCase.redeemReferralCode(eq("ABC123"), eq(newUserId))).thenReturn(
                ReferralPointsResponseDTO.builder()
                        .userId(UUID.randomUUID())
                        .totalPoints(50).weeklyPoints(50).weeklyLimit(500)
                        .pointsAwarded(50).weeklyLimitReached(false).build());

        mockMvc.perform(post("/api/social/invitations/{code}/redeem", "ABC123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("newUserId", newUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsAwarded").value(50));
    }
}
