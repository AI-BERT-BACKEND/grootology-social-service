package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.domain.ports.out.NotificationPublisherPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher implements NotificationPublisherPort {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.kafka.topic.notification-events}")
    private String notificationTopic;

    @Override
    public void publish(NotificationEvent event) {
        kafkaTemplate.send(notificationTopic, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error al publicar NotificationEvent para userId={} type={}: {}",
                                event.getUserId(), event.getType(), ex.getMessage());
                    } else {
                        log.info("NotificationEvent publicado: userId={} type={} topic={}",
                                event.getUserId(), event.getType(), notificationTopic);
                    }
                });
    }
}
