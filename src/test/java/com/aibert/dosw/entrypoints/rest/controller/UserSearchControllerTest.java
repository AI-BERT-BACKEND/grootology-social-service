package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.response.UserPresenceResponseDTO;
import com.aibert.dosw.application.dto.response.UserSearchResultDTO;
import com.aibert.dosw.domain.model.user.OnlineStatus;
import com.aibert.dosw.domain.model.user.RelationshipStatus;
import com.aibert.dosw.domain.ports.in.UserSearchUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserSearchControllerTest {

    private final UserSearchUseCase useCase = mock(UserSearchUseCase.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new UserSearchController(useCase)).build();

    @Test
    void search_conQuery_retornaResultados() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserSearchResultDTO dto = UserSearchResultDTO.builder()
                .userId(targetId).name("Ana").email("ana@test.com")
                .presenceStatus(UserPresenceResponseDTO.builder()
                        .userId(targetId).status(OnlineStatus.ONLINE).build())
                .relationshipStatus(RelationshipStatus.NONE)
                .build();

        when(useCase.search("ana", requesterId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/social/users/search")
                        .param("q", "ana")
                        .param("requesterId", requesterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].relationshipStatus").value("NONE"));
    }

    @Test
    void search_sinResultados_retornaListaVacia() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(useCase.search("xyz", requesterId)).thenReturn(List.of());

        mockMvc.perform(get("/api/social/users/search")
                        .param("q", "xyz")
                        .param("requesterId", requesterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
