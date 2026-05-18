package com.aibert.dosw.infrastructure.external.email;

import com.aibert.dosw.domain.ports.out.SocialEmailServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpSocialEmailService implements SocialEmailServicePort {

    private final JavaMailSender mailSender;

    @Override
    public void sendInvitationEmail(String toEmail, String inviterName, String registrationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Te invitan a unirse a EciPlanner");
        message.setText("Tu amigo " + inviterName + " te invita a EciPlanner.\n\nRegístrate aquí: " + registrationLink);
        mailSender.send(message);
    }
}
