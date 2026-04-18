package com.example.store.payment.service;

import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentByOrderId(UUID orderId);
}
