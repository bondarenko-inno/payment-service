package org.ebndrnk.paymentservice.integration;

import org.ebndrnk.paymentservice.config.KafkaConsumerConfig;
import org.ebndrnk.paymentservice.config.KafkaProducerConfig;
import org.ebndrnk.paymentservice.config.TestRestTemplateConfig;
import org.ebndrnk.paymentservice.model.document.Payment;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.ebndrnk.paymentservice.model.dto.TotalSumRequest;
import org.ebndrnk.paymentservice.model.dto.TotalSumResponse;
import org.ebndrnk.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestRestTemplateConfig.class)
@OverrideAutoConfiguration(enabled = true)
class PaymentControllerIntegrationTest {

    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private KafkaProducerConfig kafkaProducerConfig;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));



    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        payment1 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId("order-1")
                .userId("user-1")
                .status(PaymentStatus.SUCCESS)
                .timestamp(Instant.now())
                .paymentAmount(BigDecimal.valueOf(100))
                .build();

        payment2 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId("order-2")
                .userId("user-1")
                .status(PaymentStatus.FAILED)
                .timestamp(Instant.now())
                .paymentAmount(BigDecimal.valueOf(200))
                .build();

        paymentRepository.saveAll(List.of(payment1, payment2));
    }


    @Test
    void shouldReturnPaymentsByOrderId() {
        ResponseEntity<PaymentResponse[]> response = restTemplate.getForEntity(
                "/payments?orderId=" + payment1.getOrderId(),
                PaymentResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody()[0].orderId()).isEqualTo("order-1");
    }

    @Test
    void shouldReturnPaymentsByUserId() {
        ResponseEntity<PaymentResponse[]> response = restTemplate.getForEntity(
                "/payments?userId=user-1",
                PaymentResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnPaymentsByStatus() {
        ResponseEntity<PaymentResponse[]> response = restTemplate.getForEntity(
                "/payments?status=FAILED",
                PaymentResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody()[0].status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldReturnTotalSumPaymentsForPeriod() {
        TotalSumRequest request = new TotalSumRequest(
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600)
        );

        ResponseEntity<TotalSumResponse> response = restTemplate.postForEntity(
                "/payments/total",
                request,
                TotalSumResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
    }
}


