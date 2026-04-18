package com.example.store.payment.service.support;

import com.example.store.payment.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewaySimulator {

    private static final long SIMULATED_DELAY_MS = 300L;
    private static final double RANDOM_SUCCESS_THRESHOLD = 0.8;

    /**
     * Имитация ответа платёжного шлюза: карта на 0000 — успех, на 9999 — отказ, иначе случайный исход.
     */
    public boolean requestAuthorization(PaymentRequest request) {
        simulateNetworkDelay();

        if (request.getCardNumber().endsWith("0000")) {
            return true;
        } else if (request.getCardNumber().endsWith("9999")) {
            return false;
        }

        return Math.random() < RANDOM_SUCCESS_THRESHOLD;
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
