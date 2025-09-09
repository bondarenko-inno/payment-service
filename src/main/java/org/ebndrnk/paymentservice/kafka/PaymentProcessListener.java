package org.ebndrnk.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.paymentservice.model.dto.PaymentRequest;
import org.ebndrnk.paymentservice.service.PaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!kafka")
public class PaymentProcessListener {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order.create",
            groupId = "payment-service-group",
            containerFactory = "paymentRequestListenerContainerFactory"
    )
    public void handleOrderCreated(PaymentRequest paymentRequest, Acknowledgment acknowledgment) {
        try {
            paymentService.createPayment(paymentRequest);
            log.info("OrderCreated event consumed: {}", paymentRequest);
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
