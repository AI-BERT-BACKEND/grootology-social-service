package com.aibert.dosw.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String userId;
    private String type;
    private String title;
    private String message;
    private String severity;
    private String relatedEntityId;
}
