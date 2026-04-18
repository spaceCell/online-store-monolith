package com.example.store.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private LocalDateTime processedAt;
}