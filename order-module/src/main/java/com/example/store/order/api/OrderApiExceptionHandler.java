package com.example.store.order.api;

import com.example.store.order.exception.OrderNotFoundException;
import com.example.store.order.exception.OrderProcessingException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Hidden
@RestControllerAdvice(basePackageClasses = OrderController.class)
@Slf4j
public class OrderApiExceptionHandler {

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ErrorBody> handleOrderProcessing(OrderProcessingException ex) {
        log.warn("Order processing failed: {}", ex.getMessage());
        ErrorBody body = new ErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                "Order Processing Failed",
                ex.getMessage(),
                LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorBody> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorBody body = new ErrorBody(
                HttpStatus.NOT_FOUND.value(),
                "Order Not Found",
                ex.getMessage(),
                LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorBody body = new ErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public record ErrorBody(int status, String error, String message, String timestamp) {
    }
}
