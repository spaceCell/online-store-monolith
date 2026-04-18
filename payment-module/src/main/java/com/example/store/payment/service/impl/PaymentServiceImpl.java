package com.example.store.payment.service.impl;

import com.example.store.payment.domain.Payment;
import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;
import com.example.store.payment.exception.PaymentException;
import com.example.store.payment.mapper.PaymentMapper;
import com.example.store.payment.repository.PaymentRepository;
import com.example.store.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}, amount: {}",
                request.getOrderId(), request.getAmount());

        validatePaymentData(request);

        Payment payment = paymentRepository.save(paymentMapper.toEntity(request));

        try {
            boolean paymentSuccess = simulatePaymentProcessing(request);

            if (paymentSuccess) {
                String transactionId = generateTransactionId();
                payment.markAsPaid(transactionId);
                payment = paymentRepository.save(payment);

                log.info("Payment successful for order: {}, transaction: {}",
                        request.getOrderId(), transactionId);
            } else {
                payment.markAsFailed();
                payment = paymentRepository.save(payment);

                log.error("Payment failed for order: {}", request.getOrderId());
                throw new PaymentException("Payment processing failed");
            }

        } catch (Exception e) {
            payment.markAsFailed();
            paymentRepository.save(payment);
            throw new PaymentException("Payment error: " + e.getMessage());
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));

        return paymentMapper.toResponse(payment);
    }

    private void validatePaymentData(PaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
            throw new PaymentException("Invalid card number");
        }

        if (request.getExpiryDate() == null || !request.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            throw new PaymentException("Invalid expiry date format (MM/YY)");
        }

        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
            throw new PaymentException("Invalid CVV");
        }

        try {
            String[] parts = request.getExpiryDate().split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);

            LocalDateTime now = LocalDateTime.now();
            if (year < now.getYear() ||
                    (year == now.getYear() && month < now.getMonthValue())) {
                throw new PaymentException("Card has expired");
            }
        } catch (Exception e) {
            throw new PaymentException("Invalid expiry date");
        }
    }

    private boolean simulatePaymentProcessing(PaymentRequest request) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (request.getCardNumber().endsWith("0000")) {
            return true;
        } else if (request.getCardNumber().endsWith("9999")) {
            return false;
        }

        return Math.random() < 0.8;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
