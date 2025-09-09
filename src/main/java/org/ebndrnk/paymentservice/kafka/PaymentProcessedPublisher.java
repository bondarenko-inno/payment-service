package org.ebndrnk.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessedPublisher {

    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    private static final String TOPIC = "payment.create";

    public void publishPaymentProcessed(PaymentResponse event) {
        try {
            kafkaTemplate.send(TOPIC, event.userId(), event);
            log.info("PaymentProcessed event published: userId={}, orderId={}, status={}",
                    event.userId(), event.orderId(), event.status());
        } catch (Exception e) {
            throw new KafkaException("Failed to send event for userId: " + event.userId(), e);
        }
    }
}
