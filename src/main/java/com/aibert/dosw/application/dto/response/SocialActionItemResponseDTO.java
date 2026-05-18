package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.user.SocialAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialActionItemResponseDTO {
    private SocialAction action;
    private String label;
    private String endpoint;
    private String method;
    private String description;
}
