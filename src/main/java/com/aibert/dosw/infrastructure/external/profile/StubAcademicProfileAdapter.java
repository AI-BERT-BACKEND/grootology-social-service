package com.aibert.dosw.infrastructure.external.profile;

import com.aibert.dosw.domain.ports.out.AcademicProfilePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StubAcademicProfileAdapter implements AcademicProfilePort {

    @Override
    public boolean isProfileComplete(UUID userId) {
        // Pendiente integración con servicio AIB-6 de perfil académico.
        // En producción se reemplaza con llamada HTTP al servicio de perfiles.
        return true;
    }
}
