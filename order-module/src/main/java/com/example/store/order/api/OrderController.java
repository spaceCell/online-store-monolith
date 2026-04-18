package com.example.store.order.api;

import com.example.store.order.domain.Order;
import com.example.store.order.dto.OrderRequest;
import com.example.store.order.dto.OrderResponse;
import com.example.store.order.exception.OrderNotFoundException;
import com.example.store.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("REST request to create order for user: {}", request.getUserId());

        // Создаем заказ и сразу преобразуем в DTO
        Order order = orderService.createOrder(request);
        OrderResponse response = OrderResponse.fromEntity(order);

        // Добавляем информацию о платеже
        try {
            var payment = orderService.getPaymentByOrderId(order.getId());
            response.setPaymentStatus(payment.getStatus());
            response.setTransactionId(payment.getTransactionId());
        } catch (Exception e) {
            log.warn("Payment info not available for order: {}", order.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        log.info("REST request to get order by id: {}", id);

        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id, @RequestParam String reason) {
        log.info("REST request to cancel order {}: {}", id, reason);

        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Order Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private final String timestamp = java.time.LocalDateTime.now().toString();
    }
}
