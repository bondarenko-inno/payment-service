package org.ebndrnk.paymentservice.unit;

import org.ebndrnk.paymentservice.exception.dto.InvalidPaymentAmountException;
import org.ebndrnk.paymentservice.exception.dto.InvalidPeriodException;
import org.ebndrnk.paymentservice.exception.dto.PaymentNotFoundException;
import org.ebndrnk.paymentservice.kafka.PaymentProcessedPublisher;
import org.ebndrnk.paymentservice.mapper.PaymentMapper;
import org.ebndrnk.paymentservice.model.document.Payment;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.ebndrnk.paymentservice.model.dto.PaymentRequest;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.ebndrnk.paymentservice.model.dto.TotalSumRequest;
import org.ebndrnk.paymentservice.model.dto.TotalSumResponse;
import org.ebndrnk.paymentservice.repository.PaymentRepository;
import org.ebndrnk.paymentservice.service.PaymentService;
import org.ebndrnk.paymentservice.service.pay.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PaymentGateway paymentGateway;
    @Mock private PaymentProcessedPublisher paymentProcessedPublisher;

    @InjectMocks private PaymentService paymentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void createPayment_shouldSaveAndPublishPayment() {
        PaymentRequest request = new PaymentRequest(String.valueOf(new Random().nextInt(10)), "user1", BigDecimal.valueOf(100));
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(request.orderId())
                .userId(request.userId())
                .status(PaymentStatus.SUCCESS)
                .paymentAmount(request.paymentAmount())
                .timestamp(Instant.now())
                .build();
        PaymentResponse response = new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getUserId(), payment.getStatus(), payment.getTimestamp(), payment.getPaymentAmount());

        when(paymentGateway.processPayment()).thenReturn(PaymentStatus.SUCCESS);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.paymentToResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentProcessedPublisher).publishPaymentProcessed(response);
    }

    @Test
    void createPayment_shouldThrow_whenAmountIsZeroOrNegative() {
        PaymentRequest request = new PaymentRequest(String.valueOf(new Random().nextInt(10)), "user1", BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidPaymentAmountException.class)
                .hasMessageContaining("Payment amount must be greater than zero");
    }


    @Test
    void getPaymentsByOrderId_shouldReturnResponses() {
        String orderId = "order1";
        Payment payment = Payment.builder().id(UUID.randomUUID()).orderId(orderId).build();
        PaymentResponse response = new PaymentResponse(payment.getId(), orderId, "user1", PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of(payment));
        when(paymentMapper.paymentToResponse(payment)).thenReturn(response);

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(orderId);

        assertThat(result).hasSize(1).containsExactly(response);
    }

    @Test
    void getPaymentsByOrderId_shouldThrow_whenNoPayments() {
        when(paymentRepository.findByOrderId("order1")).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.getPaymentsByOrderId("order1"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("No payments found for orderId");
    }


    @Test
    void getPaymentsByUserId_shouldReturnResponses() {
        String userId = "user1";
        Payment payment = Payment.builder().id(UUID.randomUUID()).userId(userId).build();
        PaymentResponse response = new PaymentResponse(payment.getId(), "order1", userId, PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN);

        when(paymentRepository.findByUserIdOrderByTimestampDesc(userId)).thenReturn(List.of(payment));
        when(paymentMapper.paymentToResponse(payment)).thenReturn(response);

        List<PaymentResponse> result = paymentService.getPaymentsByUserId(userId);

        assertThat(result).hasSize(1).containsExactly(response);
    }

    @Test
    void getPaymentsByUserId_shouldThrow_whenNoPayments() {
        when(paymentRepository.findByUserIdOrderByTimestampDesc("user1")).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.getPaymentsByUserId("user1"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("No payments found for userId");
    }


    @Test
    void getPaymentsByStatus_shouldReturnResponses() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).status(PaymentStatus.SUCCESS).build();
        PaymentResponse response = new PaymentResponse(payment.getId(), "order1", "user1", PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN);

        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(List.of(payment));
        when(paymentMapper.paymentToResponse(payment)).thenReturn(response);

        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS);

        assertThat(result).hasSize(1).containsExactly(response);
    }

    @Test
    void getPaymentsByStatus_shouldThrow_whenNoPayments() {
        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("No payments found with status");
    }


    @Test
    void getTotalSumPaymentsForPeriod_shouldReturnTotal() {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");
        TotalSumRequest request = new TotalSumRequest(from, to);

        Payment p1 = Payment.builder().paymentAmount(BigDecimal.valueOf(100)).build();
        Payment p2 = Payment.builder().paymentAmount(BigDecimal.valueOf(200)).build();

        when(paymentRepository.findPaymentsInPeriod(from, to)).thenReturn(List.of(p1, p2));

        TotalSumResponse result = paymentService.getTotalSumPaymentsForPeriod(request);

        assertThat(result.totalSum()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void getTotalSumPaymentsForPeriod_shouldThrow_whenInvalidPeriod() {
        Instant from = Instant.parse("2025-12-31T23:59:59Z");
        Instant to = Instant.parse("2025-01-01T00:00:00Z");
        TotalSumRequest request = new TotalSumRequest(from, to);

        assertThatThrownBy(() -> paymentService.getTotalSumPaymentsForPeriod(request))
                .isInstanceOf(InvalidPeriodException.class)
                .hasMessageContaining("'from' must be before 'to'");
    }

    @Test
    void getTotalSumPaymentsForPeriod_shouldThrow_whenNoPayments() {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");
        TotalSumRequest request = new TotalSumRequest(from, to);

        when(paymentRepository.findPaymentsInPeriod(from, to)).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.getTotalSumPaymentsForPeriod(request))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("No payments found in the given period");
    }
}
