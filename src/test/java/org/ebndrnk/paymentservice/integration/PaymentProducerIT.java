package org.ebndrnk.paymentservice.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ebndrnk.paymentservice.config.KafkaTestConfig;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { KafkaTestConfig.class })
@ActiveProfiles("kafka")
@EmbeddedKafka(partitions = 1, topics = { "payment.create" })
class PaymentProducerIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private org.springframework.kafka.core.KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    private Consumer<String, PaymentResponse> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close(Duration.ofSeconds(1));
    }

    @Test
    void sendPaymentResponse_shouldAppearInTopic() {
        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafka
        ));
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.VALUE_DEFAULT_TYPE,
                PaymentResponse.class.getName());

        consumer = new DefaultKafkaConsumerFactory<String, PaymentResponse>(consumerProps)
                .createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "payment.create");

        PaymentResponse payload = new PaymentResponse(
                 UUID.randomUUID(),
                "orderId",
                 "userId",
                PaymentStatus.SUCCESS,
                Instant.now(),
                BigDecimal.TEN
        );
        kafkaTemplate.send("payment.create", payload);

        var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertThat(records.count()).isGreaterThan(0);

        var record = records.iterator().next();
        assertThat(record.topic()).isEqualTo("payment.create");
        assertThat(record.value()).isNotNull();
        assertThat(record.value().orderId()).isEqualTo("orderId");
        assertThat(record.value().status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(record.value().paymentAmount()).isEqualTo(BigDecimal.TEN);
    }
}