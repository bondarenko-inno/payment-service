package org.ebndrnk.paymentservice.exception.dto;

import org.ebndrnk.common.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentAmountException extends BaseServiceException {
    public InvalidPaymentAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_AMOUNT");
    }
}