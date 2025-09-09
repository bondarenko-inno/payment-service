package org.ebndrnk.paymentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ebndrnk.paymentservice.model.document.PaymentStatus;
import org.ebndrnk.paymentservice.model.dto.PaymentRequest;
import org.ebndrnk.paymentservice.model.dto.PaymentResponse;
import org.ebndrnk.paymentservice.model.dto.TotalSumRequest;
import org.ebndrnk.paymentservice.model.dto.TotalSumResponse;
import org.ebndrnk.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid PaymentRequest paymentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(paymentRequest));
    }


    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) PaymentStatus status
    ) {
        if (orderId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
        } else if (userId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
        } else if (status != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/total")
    public ResponseEntity<TotalSumResponse> getTotalSumPaymentsForPeriod(@RequestBody TotalSumRequest request) {
        return ResponseEntity.ok(paymentService.getTotalSumPaymentsForPeriod(request));
    }
}
