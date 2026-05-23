package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.InviteFriendsRequestDTO;
import com.aibert.dosw.application.dto.response.InviteResponseDTO;
import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.model.user.Invitation;
import com.aibert.dosw.domain.ports.in.InvitationUseCase;
import com.aibert.dosw.domain.ports.in.ReferralPointsUseCase;
import com.aibert.dosw.domain.ports.out.InvitationRepositoryPort;
import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import com.aibert.dosw.domain.ports.out.UserPresenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService implements InvitationUseCase {

    private final InvitationRepositoryPort invitationRepository;
    private final SocialEmailServicePort emailService;
    private final UserPresenceRepositoryPort userPresenceRepository;
    private final ReferralPointsUseCase referralPointsUseCase;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public InviteResponseDTO getOrCreateReferralLink(UUID userId) {
        log.info("Getting or creating referral link for userId={}", userId);
        Invitation invitation = invitationRepository.findByInviterId(userId)
                .orElseGet(() -> {
                    log.debug("No referral link found for userId={}, creating new one", userId);
                    return invitationRepository.save(Invitation.builder()
                            .inviterId(userId)
                            .referralCode(UUID.randomUUID().toString())
                            .used(false)
                            .build());
                });

        return InviteResponseDTO.builder()
                .referralLink(baseUrl + "/register?ref=" + invitation.getReferralCode())
                .message("Comparte este enlace con usuarios externos a AI.BERT")
                .build();
    }

    @Override
    public void sendInvitations(UUID userId, InviteFriendsRequestDTO request) {
        if (request.getEmails() == null || request.getEmails().isEmpty()) return;

        log.info("Sending {} invitations for userId={}", request.getEmails().size(), userId);

        String inviterEmail = userPresenceRepository.findByUserId(userId)
                .map(p -> p.getEmail())
                .orElse(null);

        Invitation invitation = invitationRepository.findByInviterId(userId)
                .orElseGet(() -> invitationRepository.save(Invitation.builder()
                        .inviterId(userId)
                        .referralCode(UUID.randomUUID().toString())
                        .used(false)
                        .build()));

        String link = baseUrl + "/register?ref=" + invitation.getReferralCode();

        for (String email : request.getEmails()) {
            if (inviterEmail != null && email.equalsIgnoreCase(inviterEmail)) {
                log.warn("userId={} attempted to invite themselves (email={})", userId, email);
                throw new InvalidInvitationException("No puedes invitarte a ti mismo");
            }
            if (userPresenceRepository.existsByEmail(email)) {
                log.warn("Invitation rejected: email={} already registered in AI.BERT", email);
                throw new InvalidInvitationException(
                        "El correo " + email + " ya pertenece a un usuario registrado en AI.BERT. " +
                        "Usa solicitudes de conexión en su lugar: POST /api/social/connections/{senderId}/request");
            }
            emailService.sendInvitationEmail(email, userId.toString(), link);
            log.debug("Invitation email sent to={} by userId={}", email, userId);
        }
    }

    @Override
    public ReferralPointsResponseDTO redeemReferralCode(String code, UUID newUserId) {
        log.info("Redeeming referral code={} by newUserId={}", code, newUserId);
        Invitation invitation = invitationRepository.findByReferralCode(code)
                .orElseThrow(() -> {
                    log.warn("Referral code not found: code={}", code);
                    return new InvalidInvitationException("Código de referido no válido");
                });

        if (invitation.isUsed()) {
            log.warn("Referral code already used: code={}", code);
            throw new InvalidInvitationException("Este código de referido ya fue utilizado");
        }

        invitationRepository.save(Invitation.builder()
                .id(invitation.getId())
                .inviterId(invitation.getInviterId())
                .referralCode(invitation.getReferralCode())
                .inviteeEmail(invitation.getInviteeEmail())
                .used(true)
                .build());

        log.debug("Referral code={} marked as used, awarding points to inviterId={}", code, invitation.getInviterId());
        return referralPointsUseCase.awardPoints(invitation.getInviterId());
    }
}
