package com.aibert.dosw.infrastructure.external.profile;

import com.aibert.dosw.domain.ports.out.AcademicProfilePort;
import com.aibert.dosw.infrastructure.external.profile.client.ProfileServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignAcademicProfileAdapter implements AcademicProfilePort {

    private final ProfileServiceClient profileServiceClient;

    @Override
    public boolean isProfileComplete(UUID userId) {
        try {
            Object profile = profileServiceClient.getAcademicProfile(userId);
            return profile != null;
        } catch (Exception e) {
            log.warn("No se pudo consultar el perfil académico del usuario {}. Se permite el acceso por defecto. Error: {}", userId, e.getMessage());
            return true;
        }
    }
}
