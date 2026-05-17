package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.model.user.UserPresence;
import com.aibert.dosw.domain.ports.in.ReferralPointsUseCase;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock private ReferralPointsUseCase referralPointsUseCase;
    @InjectMocks private InvitationService invitationService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "baseUrl", "http://localhost:8085");
    }

    @Test
    void getOrCreateReferralLink_creaEnlaceNuevo() {
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.empty());
        when(invitationRepository.save(any())).thenReturn(Invitation.builder()
                .inviterId(userId).referralCode("test-code").used(false).build());

        InviteResponseDTO response = invitationService.getOrCreateReferralLink(userId);

        assertNotNull(response.getReferralLink());
        assertTrue(response.getReferralLink().contains("test-code"));
    }

    @Test
    void getOrCreateReferralLink_retornaEnlaceExistente() {
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(
                Invitation.builder().inviterId(userId).referralCode("existing-code").used(false).build()));

        InviteResponseDTO response = invitationService.getOrCreateReferralLink(userId);

        assertTrue(response.getReferralLink().contains("existing-code"));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void sendInvitations_exitoso_enviaCorreos() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of("externo@ejemplo.com"));

        UserPresence inviterPresence = UserPresence.builder()
                .userId(userId).email("inviter@test.com").build();
        when(userPresenceRepository.findByUserId(userId)).thenReturn(Optional.of(inviterPresence));
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(
                Invitation.builder().inviterId(userId).referralCode("code123").used(false).build()));
        when(userPresenceRepository.existsByEmail("externo@ejemplo.com")).thenReturn(false);
        doNothing().when(emailService).sendInvitationEmail(any(), any(), any());

        assertDoesNotThrow(() -> invitationService.sendInvitations(userId, request));
        verify(emailService).sendInvitationEmail(any(), any(), any());
    }

    @Test
    void sendInvitations_autoInvitacion_lanzaExcepcion() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of("inviter@test.com"));

        UserPresence inviterPresence = UserPresence.builder()
                .userId(userId).email("inviter@test.com").build();
        when(userPresenceRepository.findByUserId(userId)).thenReturn(Optional.of(inviterPresence));
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(
                Invitation.builder().inviterId(userId).referralCode("code123").used(false).build()));

        assertThrows(InvalidInvitationException.class,
                () -> invitationService.sendInvitations(userId, request));
        verify(emailService, never()).sendInvitationEmail(any(), any(), any());
    }

    @Test
    void sendInvitations_correoRegistrado_lanzaExcepcion() {
        com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO request =
                mock(com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO.class);
        when(request.getEmails()).thenReturn(List.of("registrado@aibert.com"));

        when(userPresenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(invitationRepository.findByInviterId(userId)).thenReturn(Optional.of(
                Invitation.builder().inviterId(userId).referralCode("code123").used(false).build()));
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

    @Test
    void redeemReferralCode_exitoso_otorgaPuntos() {
        UUID inviterId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID()).inviterId(inviterId)
                .referralCode("valid-code").used(false).build();

        when(invitationRepository.findByReferralCode("valid-code")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any())).thenReturn(invitation);
        when(referralPointsUseCase.awardPoints(inviterId)).thenReturn(ReferralPointsResponseDTO.builder()
                .userId(inviterId).totalPoints(50).weeklyPoints(50)
                .weeklyLimit(500).pointsAwarded(50).weeklyLimitReached(false).build());

        ReferralPointsResponseDTO result = invitationService.redeemReferralCode("valid-code", newUserId);

        assertEquals(50, result.getPointsAwarded());
        verify(invitationRepository).save(any());
        verify(referralPointsUseCase).awardPoints(inviterId);
    }

    @Test
    void redeemReferralCode_codigoInvalido_lanzaExcepcion() {
        when(invitationRepository.findByReferralCode("bad-code")).thenReturn(Optional.empty());

        assertThrows(InvalidInvitationException.class,
                () -> invitationService.redeemReferralCode("bad-code", UUID.randomUUID()));
        verifyNoInteractions(referralPointsUseCase);
    }

    @Test
    void redeemReferralCode_codigoYaUsado_lanzaExcepcion() {
        Invitation used = Invitation.builder()
                .id(UUID.randomUUID()).inviterId(UUID.randomUUID())
                .referralCode("used-code").used(true).build();
        when(invitationRepository.findByReferralCode("used-code")).thenReturn(Optional.of(used));

        assertThrows(InvalidInvitationException.class,
                () -> invitationService.redeemReferralCode("used-code", UUID.randomUUID()));
        verifyNoInteractions(referralPointsUseCase);
    }
}
