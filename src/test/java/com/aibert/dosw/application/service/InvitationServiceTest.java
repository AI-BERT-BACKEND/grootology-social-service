package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock private InvitationRepositoryPort invitationRepository;
    @Mock private SocialEmailServicePort emailService;
    @Mock private UserPresenceRepositoryPort userPresenceRepository;
    @InjectMocks private InvitationService invitationService;

    private final UUID userId = UUID.randomUUID();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "baseUrl", "http://localhost:8085");
    }

    @Test
    void getOrCreateReferralLink_creaEnlaceNuevo() {
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.empty());
        when(invitationRepository.save(any())).thenReturn(Invitation.builder()
                .inviterId(userId)
                .referralCode("test-code")
                .used(false)
                .build());

        InviteResponseDTO response = invitationService.getOrCreateReferralLink(userId);

        assertNotNull(response.getReferralLink());
        assertTrue(response.getReferralLink().contains("test-code"));
    }

    @Test
    void getOrCreateReferralLink_retornaEnlaceExistente() {
        Invitation existing = Invitation.builder()
                .inviterId(userId)
                .referralCode("existing-code")
                .used(false)
                .build();
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(existing));

        InviteResponseDTO response = invitationService.getOrCreateReferralLink(userId);

        assertTrue(response.getReferralLink().contains("existing-code"));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void sendInvitations_exitoso_enviaCorreos() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of("externo@ejemplo.com"));

        Invitation invitation = Invitation.builder()
                .inviterId(userId).referralCode("code123").used(false).build();
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(invitation));
        when(userPresenceRepository.existsByEmail("externo@ejemplo.com")).thenReturn(false);
        doNothing().when(emailService).sendInvitationEmail(any(), any(), any());

        assertDoesNotThrow(() -> invitationService.sendInvitations(userId, request));
        verify(emailService).sendInvitationEmail(any(), any(), any());
    }

    @Test
    void sendInvitations_correoRegistrado_lanzaExcepcion() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of("registrado@aibert.com"));

        Invitation invitation = Invitation.builder()
                .inviterId(userId).referralCode("code123").used(false).build();
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(invitation));
        when(userPresenceRepository.existsByEmail("registrado@aibert.com")).thenReturn(true);

        InvalidInvitationException ex = assertThrows(InvalidInvitationException.class,
                () -> invitationService.sendInvitations(userId, request));
        assertTrue(ex.getMessage().contains("registrado@aibert.com"));
        verify(emailService, never()).sendInvitationEmail(any(), any(), any());
    }

    @Test
    void sendInvitations_listaVacia_noEnviaCorreos() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of());

        assertDoesNotThrow(() -> invitationService.sendInvitations(userId, request));
        verify(emailService, never()).sendInvitationEmail(any(), any(), any());
    }

    @Test
    void sendInvitations_listaNull_noEnviaCorreos() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(null);

        assertDoesNotThrow(() -> invitationService.sendInvitations(userId, request));
        verify(emailService, never()).sendInvitationEmail(any(), any(), any());
    }
}
