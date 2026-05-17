package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService implements InvitationUseCase {

    private final InvitationRepositoryPort invitationRepository;
    private final SocialEmailServicePort emailService;
    private final UserPresenceRepositoryPort userPresenceRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public InviteResponseDTO getOrCreateReferralLink(UUID userId) {
        Invitation invitation = invitationRepository.findByInviterId(userId)
                .orElseGet(() -> invitationRepository.save(Invitation.builder()
                        .inviterId(userId)
                        .referralCode(UUID.randomUUID().toString())
                        .used(false)
                        .build()));

        return InviteResponseDTO.builder()
                .referralLink(baseUrl + "/register?ref=" + invitation.getReferralCode())
                .message("Comparte este enlace con usuarios externos a AI.BERT")
                .build();
    }

    @Override
    public void sendInvitations(UUID userId, InviteFriendsRequestDTO request) {
        if (request.getEmails() == null || request.getEmails().isEmpty()) return;

        Invitation invitation = invitationRepository.findByInviterId(userId)
                .orElseGet(() -> invitationRepository.save(Invitation.builder()
                        .inviterId(userId)
                        .referralCode(UUID.randomUUID().toString())
                        .used(false)
                        .build()));

        String link = baseUrl + "/register?ref=" + invitation.getReferralCode();

        for (String email : request.getEmails()) {
            if (userPresenceRepository.existsByEmail(email)) {
                throw new InvalidInvitationException(
                        "El correo " + email + " ya pertenece a un usuario registrado en AI.BERT. " +
                        "Usa solicitudes de conexión en su lugar: POST /api/social/connections/{senderId}/request");
            }
            emailService.sendInvitationEmail(email, userId.toString(), link);
        }
    }
}
