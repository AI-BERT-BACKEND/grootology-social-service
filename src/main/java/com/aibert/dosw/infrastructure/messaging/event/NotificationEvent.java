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
    private String type;
    private String title;
    private String message;
    private String severity;
    private UUID relatedEntityId;
}
