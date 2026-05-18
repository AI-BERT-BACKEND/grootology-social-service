package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.AvailabilityConfigRequestDTO;
import com.aibert.dosw.domain.model.user.AvailabilityConfig;
import com.aibert.dosw.domain.model.user.VisibilityLevel;
import com.aibert.dosw.domain.ports.out.AvailabilityRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock private AvailabilityRepositoryPort repository;
    @InjectMocks private AvailabilityService availabilityService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void saveConfig_validInput_savesAndReturns() {
        AvailabilityConfigRequestDTO dto = mock(AvailabilityConfigRequestDTO.class);
        when(dto.getVisibility()).thenReturn(VisibilityLevel.PUBLIC);
        when(dto.getAuthorizedFriends()).thenReturn(null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        AvailabilityConfig result = availabilityService.saveConfig(userId, dto);

        assertNotNull(result);
        assertEquals(VisibilityLevel.PUBLIC, result.getVisibility());
    }

    @Test
    void getConfig_noConfigFound_returnsPrivateDefault() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        AvailabilityConfig result = availabilityService.getConfig(userId);

        assertEquals(VisibilityLevel.PRIVATE, result.getVisibility());
    }

    @Test
    void getConfig_configExists_returnsConfig() {
        AvailabilityConfig config = AvailabilityConfig.builder()
                .userId(userId)
                .visibility(VisibilityLevel.SPECIFIC)
                .build();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(config));

        AvailabilityConfig result = availabilityService.getConfig(userId);

        assertEquals(VisibilityLevel.SPECIFIC, result.getVisibility());
    }
}
