package com.aibert.dosw.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.util.List;

@Getter
public class InviteFriendsRequestDTO {
    @Size(max = 10, message = "Máximo 10 correos por envío")
    private List<@Email String> emails;
}
