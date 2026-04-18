package com.example.store.order.dto;

import com.example.store.order.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderResponse {

    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    // Информация о платеже
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

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().toString());
        response.setCreatedAt(order.getCreatedAt());

        // Преобразуем OrderItem в OrderItemResponse (без ссылки на Order)
        if (order.getItems() != null) {
            List<OrderItemResponse> itemResponses = order.getItems().stream()
                    .map(item -> new OrderItemResponse(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getSubtotal()
                    ))
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }
}
