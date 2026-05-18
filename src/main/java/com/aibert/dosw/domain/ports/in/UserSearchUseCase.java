package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.UserSearchResultDTO;

import java.util.List;
import java.util.UUID;

public interface UserSearchUseCase {
    List<UserSearchResultDTO> search(String query, UUID requesterId);
}
