package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationEventPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate = mock(KafkaTemplate.class);
    private final NotificationEventPublisher publisher = new NotificationEventPublisher(kafkaTemplate);

    @Test
    void publish_sendsEventToTopic() {
        ReflectionTestUtils.setField(publisher, "notificationTopic", "social.events");
        when(kafkaTemplate.send(anyString(), anyString(), any(NotificationEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        NotificationEvent event = NotificationEvent.builder()
                .userId(UUID.randomUUID())
                .type("STUDY_SESSION_INVITE")
                .title("Titulo")
                .message("Mensaje")
                .severity("INFO")
                .build();

        publisher.publish(event);

        verify(kafkaTemplate).send(eq("social.events"), anyString(), eq(event));
    }
}
