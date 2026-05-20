package com.aibert.dosw.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private UUID userId;

    /**
     * Tipo del evento. Valores acordados con notification-service:
     * STUDY_SESSION_INVITE, CONNECTION_REQUEST_RECEIVED, CONNECTION_REQUEST_ACCEPTED
     */
    private String type;

    private String title;
    private String message;

    /** HIGH | MEDIUM | LOW | INFO */
    private String severity;

    private UUID relatedEntityId;
}
