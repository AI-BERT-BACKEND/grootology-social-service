package com.aibert.dosw.infrastructure.external.profile;

import com.aibert.dosw.domain.ports.out.AcademicProfilePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Stub kept as reference — replaced by FeignAcademicProfileAdapter in production.
public class StubAcademicProfileAdapter implements AcademicProfilePort {

    @Override
    public boolean isProfileComplete(UUID userId) {
        // TODO: replace with HTTP call to AIB-6 profile service in production.
        return true;
    }
}
