package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService implements InvitationUseCase {

    private final InvitationRepositoryPort invitationRepository;
    private final SocialEmailServicePort emailService;

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
                .message("Comparte este enlace con tus amigos")
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
            if (email.equals(userId.toString())) {
                throw new InvalidInvitationException("No puedes invitarte a ti mismo");
            }
            emailService.sendInvitationEmail(email, userId.toString(), link);
        }
    }
}
