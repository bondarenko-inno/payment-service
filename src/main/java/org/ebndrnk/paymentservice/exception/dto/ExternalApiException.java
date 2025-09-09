package org.ebndrnk.paymentservice.exception.dto;

import org.ebndrnk.paymentservice.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class ExternalApiException extends BaseServiceException {
    public ExternalApiException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_API_ERROR");
    }
}