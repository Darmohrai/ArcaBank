package com.arcabank.core_finance.exception;

import com.arcabank.core_finance.utils.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getErrorCode().getStatus(),
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://api.arcabank.com/errors/" + ex.getErrorCode().name().toLowerCase()));
        problemDetail.setTitle(ex.getErrorCode().name());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR.getDefaultMessage()
        );
        problemDetail.setType(URI.create("https://api.arcabank.com/errors/validation-error"));
        problemDetail.setTitle(ErrorCode.VALIDATION_ERROR.name());

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Невірне значення",
                (existing, replacement) -> existing
            ));

        problemDetail.setProperty("invalid_fields", fieldErrors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR.getDefaultMessage()
        );
        problemDetail.setType(URI.create("https://api.arcabank.com/errors/internal-server-error"));
        problemDetail.setTitle(ErrorCode.INTERNAL_ERROR.name());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
