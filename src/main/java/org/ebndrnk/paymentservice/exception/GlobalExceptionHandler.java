package org.ebndrnk.paymentservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST controllers.
 * <p>
 * Handles specific custom exceptions and returns structured error responses
 * with appropriate HTTP status codes and error details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseServiceException.class)
    public ResponseEntity<ErrorInfo> handleBaseServiceException(BaseServiceException ex, HttpServletRequest request) {

        ErrorInfo errorInfo = new ErrorInfo(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorInfo, ex.getStatus());
    }
}