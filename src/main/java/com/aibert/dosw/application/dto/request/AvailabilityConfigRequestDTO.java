package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.user.VisibilityLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
public class AvailabilityConfigRequestDTO {
    @NotNull
    private VisibilityLevel visibility;
    private List<UUID> authorizedFriends;
}
