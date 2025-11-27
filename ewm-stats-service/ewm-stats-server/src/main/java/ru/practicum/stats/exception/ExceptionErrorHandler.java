package ru.practicum.stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class ExceptionErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ConstraintViolationException e) {
        log.warn("ConstraintViolationException: {}", e.getMessage());
        return new ErrorResponse("Validation failed: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDateTimeParseException(DateTimeParseException e) {
        log.warn("DateTimeParseException: {}", e.getMessage());
        return new ErrorResponse("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Exception e) {
        log.error("Unexpected error: ", e);
        return new ErrorResponse("Internal server error occurred.");
    }

    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}