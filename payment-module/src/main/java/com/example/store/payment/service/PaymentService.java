package com.example.store.payment.service;

import com.example.store.payment.domain.Payment;
import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;
import com.example.store.payment.exception.PaymentException;
import com.example.store.payment.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Процессинг платежа при создании заказа
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}, amount: {}",
                request.getOrderId(), request.getAmount());

        // 1. Валидация платежных данных
        validatePaymentData(request);

        // 2. Создаем запись о платеже
        Payment payment = new Payment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount()
        );
        payment = paymentRepository.save(payment);

        try {
            // 3. Имитация обработки платежа
            boolean paymentSuccess = simulatePaymentProcessing(request);

            if (paymentSuccess) {
                // 4. Успешная оплата
                String transactionId = generateTransactionId();
                payment.markAsPaid(transactionId);
                payment = paymentRepository.save(payment);

                log.info("Payment successful for order: {}, transaction: {}",
                        request.getOrderId(), transactionId);
            } else {
                // 5. Ошибка оплаты
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

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Проверка статуса платежа по заказу
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));

        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Проверка, оплачен ли заказ
     */
    @Transactional(readOnly = true)
    public boolean isOrderPaid(UUID orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, Payment.PaymentStatus.PAID);
    }

    /**
     * Валидация данных карты
     */
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

        // Проверка срока действия
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

    /**
     * Имитация обработки платежа (80% успешных)
     */
    private boolean simulatePaymentProcessing(PaymentRequest request) {
        try {
            Thread.sleep(300); // Имитация задержки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Имитация успеха/неудачи
        // Для тестирования: карта, оканчивающаяся на 0000 всегда успешна
        // карта, оканчивающаяся на 9999 всегда неуспешна
        if (request.getCardNumber().endsWith("0000")) {
            return true;
        } else if (request.getCardNumber().endsWith("9999")) {
            return false;
        }

        // Случайный результат для остальных карт (80% успех)
        return Math.random() < 0.8;
    }

    /**
     * Генерация ID транзакции
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
