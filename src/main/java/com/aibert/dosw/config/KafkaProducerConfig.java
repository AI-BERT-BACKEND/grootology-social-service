package com.aibert.dosw.config;

import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Configuracion del productor de Kafka.
 *
 * <p>El {@link ProducerFactory} se construye a partir de las propiedades de
 * Spring Boot ({@code spring.kafka.*} de application.yml). De esta forma se
 * aplican TODAS las propiedades configuradas, incluidas las de seguridad
 * {@code SASL_SSL} necesarias para el Event Hub de Azure en los perfiles
 * {@code qa} y {@code prod}.</p>
 *
 * <p>Importante: NO se debe volver a construir el mapa de propiedades a mano,
 * porque eso ignoraria {@code spring.kafka.properties.*} (security.protocol,
 * sasl.mechanism, sasl.jaas.config) y la conexion al Event Hub fallaria.</p>
 */
@Configuration
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, NotificationEvent> notificationProducerFactory() {
        Map<String, Object> config = kafkaProperties.buildProducerProperties(null);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }
}
