package org.ebndrnk.paymentservice.exception.dto;

import org.ebndrnk.paymentservice.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseServiceException {
    public PaymentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
    }
}