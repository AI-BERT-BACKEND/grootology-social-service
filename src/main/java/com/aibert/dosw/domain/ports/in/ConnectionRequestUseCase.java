package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ConnectionRequestUseCase {
    ConnectionRequestResponseDTO sendRequest(UUID senderId, SendConnectionRequestDTO request);
    ConnectionRequestResponseDTO acceptRequest(UUID requestId, UUID receiverId);
    ConnectionRequestResponseDTO rejectRequest(UUID requestId, UUID receiverId);
    List<ConnectionRequestResponseDTO> getPendingRequestsForUser(UUID receiverId);
    List<ConnectionRequestResponseDTO> getSentRequestsByUser(UUID senderId);
}
