package org.ebndrnk.paymentservice.exception.dto;

import org.ebndrnk.common.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class InvalidPeriodException extends BaseServiceException {
    public InvalidPeriodException(String message) { super(message, HttpStatus.BAD_REQUEST, "INVALID_PERIOD"); }
}