package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.SocialActionItemResponseDTO;
import com.aibert.dosw.application.dto.response.SocialPanelResponseDTO;
import com.aibert.dosw.domain.model.user.SocialAction;
import com.aibert.dosw.domain.ports.in.SocialPanelUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SocialPanelControllerTest {

    @Test
    void getPanel_conAccion_enumEsProcesado() throws Exception {
        UUID userId = UUID.randomUUID();
        SocialPanelUseCase useCase = mock(SocialPanelUseCase.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SocialPanelController(useCase)).build();

        when(useCase.getPanel(eq(userId), eq(SocialAction.INVITAR))).thenReturn(SocialPanelResponseDTO.builder()
                .userId(userId)
                .selectedAction(SocialAction.INVITAR)
                .availableActions(List.of(
                        SocialActionItemResponseDTO.builder()
                                .action(SocialAction.INVITAR)
                                .label("Invitar")
                                .endpoint("/api/social/invitations/" + userId + "/send")
                                .method("POST")
                                .description("Envio de invitaciones a otros usuarios.")
                                .build()))
                .build());

        mockMvc.perform(get("/api/social/users/{userId}/panel", userId)
                        .param("action", "INVITAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedAction").value("INVITAR"))
                .andExpect(jsonPath("$.availableActions[0].action").value("INVITAR"));

        verify(useCase).getPanel(eq(userId), eq(SocialAction.INVITAR));
    }
}
