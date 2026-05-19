package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;

public interface NotificationPublisherPort {
    void publish(NotificationEvent event);
}
