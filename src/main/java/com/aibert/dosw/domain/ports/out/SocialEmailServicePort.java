package com.aibert.dosw.domain.ports.out;

public interface SocialEmailServicePort {
    void sendInvitationEmail(String toEmail, String inviterName, String registrationLink);
}
