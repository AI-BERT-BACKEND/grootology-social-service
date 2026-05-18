package com.aibert.dosw.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InviteResponseDTO {
    private String referralLink;
    private String message;
}
