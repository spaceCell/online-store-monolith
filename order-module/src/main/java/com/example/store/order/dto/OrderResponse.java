package com.example.store.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderResponse {

    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    private String paymentStatus;
    private String transactionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}
