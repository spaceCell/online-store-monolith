package com.example.store.payment.dto;

import com.example.store.payment.domain.Payment;
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

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus().toString());
        response.setTransactionId(payment.getTransactionId());
        response.setProcessedAt(payment.getProcessedAt());
        return response;
    }
}