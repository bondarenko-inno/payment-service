package org.ebndrnk.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${environment.kafka.address}")
    private String bootstrapServers;

    @Bean
    @Profile("!kafka")
    public NewTopic paymentCreatedTopic() {
        return TopicBuilder.name("payment.create")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @Profile("!kafka")
    public ProducerFactory<String, PaymentResponse> paymentProcessedProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @Profile("!kafka")
    public KafkaTemplate<String, PaymentResponse> paymentProcessedKafkaTemplate() {
        KafkaTemplate<String, PaymentResponse> template =
                new KafkaTemplate<>(paymentProcessedProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }
}
