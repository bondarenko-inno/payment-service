package org.ebndrnk.paymentservice.service;

import lombok.RequiredArgsConstructor;
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
import org.ebndrnk.paymentservice.service.pay.PaymentGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;
    private final PaymentProcessedPublisher paymentProcessedPublisher;


    public PaymentResponse createPayment(PaymentRequest request) {
        if (request.paymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("Payment amount must be greater than zero");
        }

        PaymentStatus status = paymentGateway.processPayment();
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(request.orderId())
                .userId(request.userId())
                .status(status)
                .timestamp(Instant.now())
                .paymentAmount(request.paymentAmount())
                .build();

        PaymentResponse response = paymentMapper.paymentToResponse(paymentRepository.save(payment));
        paymentProcessedPublisher.publishPaymentProcessed(response);
        return response;
    }


    public List<PaymentResponse> getPaymentsByOrderId(String orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException("No payments found for orderId: " + orderId);
        }
        return payments.stream()
                .map(paymentMapper::paymentToResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByUserId(String userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByTimestampDesc(userId);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException("No payments found for userId: " + userId);
        }
        return payments.stream()
                .map(paymentMapper::paymentToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException("No payments found with status: " + status);
        }
        return payments.stream()
                .map(paymentMapper::paymentToResponse)
                .collect(Collectors.toList());
    }


    public TotalSumResponse getTotalSumPaymentsForPeriod(TotalSumRequest request) {
        if (request.from().isAfter(request.to())) {
            throw new InvalidPeriodException("'from' must be before 'to'");
        }

        List<Payment> payments = paymentRepository.findPaymentsInPeriod(request.from(), request.to());
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException("No payments found in the given period");
        }

        BigDecimal total = payments.stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalSumResponse(total);
    }
}
