package com.example.store.payment.service.impl;

import com.example.store.payment.domain.Payment;
import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;
import com.example.store.payment.exception.PaymentException;
import com.example.store.payment.mapper.PaymentMapper;
import com.example.store.payment.repository.PaymentRepository;
import com.example.store.payment.service.PaymentService;
import com.example.store.payment.service.support.PaymentCardValidator;
import com.example.store.payment.service.support.PaymentGatewaySimulator;
import com.example.store.payment.service.support.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentCardValidator paymentCardValidator;
    private final PaymentGatewaySimulator paymentGatewaySimulator;
    private final TransactionIdGenerator transactionIdGenerator;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}, amount: {}",
                request.getOrderId(), request.getAmount());

        paymentCardValidator.validate(request);

        Payment payment = paymentMapper.toEntity(request);
        persistPayment(payment);

        boolean paymentSuccess = paymentGatewaySimulator.requestAuthorization(request);
        if (paymentSuccess) {
            String txnId = transactionIdGenerator.generate();
            payment.markAsPaid(txnId);
            persistPayment(payment);

            log.info("Payment successful for order: {}, transaction: {}",
                    request.getOrderId(), txnId);
        } else {
            payment.markAsFailed();
            persistPayment(payment);

            log.error("Payment failed for order: {}", request.getOrderId());
            throw new PaymentException("Payment processing failed");
        }

        return paymentMapper.toResponse(payment);
    }

    private void persistPayment(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));

        return paymentMapper.toResponse(payment);
    }
}
