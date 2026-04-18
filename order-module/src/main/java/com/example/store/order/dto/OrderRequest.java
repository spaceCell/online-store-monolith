package com.example.store.order.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {

    private UUID userId;
    private List<OrderItemRequest> items;
    private PaymentInfo paymentInfo; // Добавляем платежную информацию

    @Data
    public static class OrderItemRequest {
        private UUID productId;
        private Integer quantity;
    }

    @Data
    public static class PaymentInfo {
        private String cardNumber;
        private String cardHolderName;
        private String expiryDate;
        private String cvv;
    }
}
