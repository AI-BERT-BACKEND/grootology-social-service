package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.user.VisibilityLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
public class AvailabilityConfigRequestDTO {
    @NotNull
    @Schema(example = "PUBLIC")
    private VisibilityLevel visibility;

    @Schema(example = "[\"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"b2c3d4e5-f6a7-8901-bcde-f12345678901\"]")
    private List<UUID> authorizedFriends;
}
