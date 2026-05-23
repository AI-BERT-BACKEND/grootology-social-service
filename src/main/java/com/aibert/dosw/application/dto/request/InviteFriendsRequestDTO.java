package com.aibert.dosw.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import java.util.List;

@Getter
public class InviteFriendsRequestDTO {
    @Size(max = 10, message = "Máximo 10 correos por envío")
    @Schema(example = "[\"carlos.perez@gmail.com\", \"maria.lopez@hotmail.com\"]")
    private List<@Email String> emails;
}
