package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.user.Invitation;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepositoryPort {
    Invitation save(Invitation invitation);
    Optional<Invitation> findByReferralCode(String code);
    Optional<Invitation> findByInviterId(UUID inviterId);
}
