package org.ebndrnk.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseServiceException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    protected BaseServiceException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

}