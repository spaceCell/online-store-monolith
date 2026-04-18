package com.example.store.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {

    @Id
    @UuidGenerator
    private UUID id;

    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED
    }

    public Payment(UUID orderId, UUID userId, BigDecimal amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsPaid(String transactionId) {
        this.status = PaymentStatus.PAID;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
}
