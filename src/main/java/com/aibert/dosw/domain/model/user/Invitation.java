package com.aibert.dosw.domain.model.user;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Invitation {
    private UUID id;
    private UUID inviterId;
    private String referralCode;
    private String inviteeEmail;
    private boolean used;
}
