package com.aibert.dosw.domain.ports.out;

import java.util.UUID;

public interface AcademicProfilePort {
    boolean isProfileComplete(UUID userId);
}
