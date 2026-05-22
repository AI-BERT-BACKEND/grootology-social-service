package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.request.SendConnectionRequestDTO;
import com.aibert.dosw.application.dto.response.ConnectionRequestResponseDTO;
import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.model.user.ConnectionRequest;
import com.aibert.dosw.domain.model.user.ConnectionRequestStatus;
import com.aibert.dosw.domain.model.user.Friendship;
import com.aibert.dosw.domain.exceptions.IncompleteProfileException;
import com.aibert.dosw.domain.ports.in.ConnectionRequestUseCase;
import com.aibert.dosw.domain.ports.out.AcademicProfilePort;
import com.aibert.dosw.domain.ports.out.ConnectionRequestRepositoryPort;
import com.aibert.dosw.domain.ports.out.FriendshipRepositoryPort;
import com.aibert.dosw.domain.ports.out.NotificationPublisherPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionRequestService implements ConnectionRequestUseCase {

    private final ConnectionRequestRepositoryPort requestRepository;
    private final FriendshipRepositoryPort friendshipRepository;
    private final AcademicProfilePort academicProfilePort;
    private final NotificationPublisherPort notificationPublisher;

    @Override
    public ConnectionRequestResponseDTO sendRequest(UUID senderId, SendConnectionRequestDTO dto) {
        if (!academicProfilePort.isProfileComplete(senderId)) {
            throw new IncompleteProfileException(
                    "Debes completar tu perfil académico antes de enviar solicitudes de conexión");
        }

        UUID receiverId = dto.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new ConnectionRequestException("No puedes enviarte una solicitud de conexión a ti mismo");
        }

        if (friendshipRepository.existsByUserIds(senderId, receiverId)) {
            throw new ConnectionRequestException("Ya son amigos");
        }

        if (requestRepository.existsByEitherDirectionAndStatus(senderId, receiverId, ConnectionRequestStatus.PENDING)) {
            throw new ConnectionRequestException("Ya existe una solicitud de conexión pendiente con este usuario");
        }

        ConnectionRequest saved = requestRepository.save(ConnectionRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(ConnectionRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build());

        try {
            notificationPublisher.publish(NotificationEvent.builder()
                    .userId(receiverId)
                    .type("CONNECTION_REQUEST_RECEIVED")
                    .title("Nueva solicitud de conexión")
                    .message("Tienes una nueva solicitud de conexión")
                    .severity("INFO")
                    .relatedEntityId(saved.getId())
                    .build());
        } catch (Exception e) {
            log.error("Error publicando notificación de solicitud para receiverId={}: {}", receiverId, e.getMessage());
        }

        return toDTO(saved);
    }

    @Override
    public ConnectionRequestResponseDTO acceptRequest(UUID requestId, UUID receiverId) {
        ConnectionRequest existing = requestRepository.findById(requestId)
                .orElseThrow(() -> new ConnectionRequestException("Solicitud de conexión no encontrada"));

        if (!existing.getReceiverId().equals(receiverId)) {
            throw new ConnectionRequestException("No tienes permiso para aceptar esta solicitud");
        }
        if (existing.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ConnectionRequestException("La solicitud ya fue procesada");
        }

        ConnectionRequestResponseDTO result = toDTO(requestRepository.save(ConnectionRequest.builder()
                .id(existing.getId())
                .senderId(existing.getSenderId())
                .receiverId(existing.getReceiverId())
                .status(ConnectionRequestStatus.ACCEPTED)
                .sentAt(existing.getSentAt())
                .build()));

        friendshipRepository.save(Friendship.builder()
                .userId1(existing.getSenderId())
                .userId2(existing.getReceiverId())
                .createdAt(LocalDateTime.now())
                .build());

        try {
            notificationPublisher.publish(NotificationEvent.builder()
                    .userId(existing.getSenderId())
                    .type("CONNECTION_REQUEST_ACCEPTED")
                    .title("Solicitud de conexión aceptada")
                    .message("Tu solicitud de conexión fue aceptada")
                    .severity("INFO")
                    .relatedEntityId(requestId)
                    .build());
        } catch (Exception e) {
            log.error("Error publicando notificación de aceptación para senderId={}: {}", existing.getSenderId(), e.getMessage());
        }

        return result;
    }

    @Override
    public ConnectionRequestResponseDTO rejectRequest(UUID requestId, UUID receiverId) {
        ConnectionRequest existing = requestRepository.findById(requestId)
                .orElseThrow(() -> new ConnectionRequestException("Solicitud de conexión no encontrada"));

        if (!existing.getReceiverId().equals(receiverId)) {
            throw new ConnectionRequestException("No tienes permiso para rechazar esta solicitud");
        }
        if (existing.getStatus() != ConnectionRequestStatus.PENDING) {
            throw new ConnectionRequestException("La solicitud ya fue procesada");
        }

        return toDTO(requestRepository.save(ConnectionRequest.builder()
                .id(existing.getId())
                .senderId(existing.getSenderId())
                .receiverId(existing.getReceiverId())
                .status(ConnectionRequestStatus.REJECTED)
                .sentAt(existing.getSentAt())
                .build()));
    }

    @Override
    public List<ConnectionRequestResponseDTO> getPendingRequestsForUser(UUID receiverId) {
        return requestRepository.findByReceiverIdAndStatus(receiverId, ConnectionRequestStatus.PENDING)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ConnectionRequestResponseDTO> getSentRequestsByUser(UUID senderId) {
        return requestRepository.findBySenderId(senderId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ConnectionRequestResponseDTO toDTO(ConnectionRequest r) {
        return ConnectionRequestResponseDTO.builder()
                .id(r.getId())
                .senderId(r.getSenderId())
                .receiverId(r.getReceiverId())
                .status(r.getStatus())
                .sentAt(r.getSentAt())
                .build();
    }
}
