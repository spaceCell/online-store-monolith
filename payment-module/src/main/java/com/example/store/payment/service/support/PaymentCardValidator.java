package com.example.store.payment.service.support;

import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.exception.PaymentException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentCardValidator {

    private static final int MIN_CARD_NUMBER_LENGTH = 16;
    private static final int EXPIRY_CENTURY = 2000;

    public void validate(PaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().length() < MIN_CARD_NUMBER_LENGTH) {
            throw new PaymentException("Invalid card number");
        }

        if (request.getExpiryDate() == null || !request.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            throw new PaymentException("Invalid expiry date format (MM/YY)");
        }

        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
            throw new PaymentException("Invalid CVV");
        }

        int month;
        int year;
        try {
            String[] parts = request.getExpiryDate().split("/");
            month = Integer.parseInt(parts[0]);
            year = EXPIRY_CENTURY + Integer.parseInt(parts[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new PaymentException("Invalid expiry date");
        }

        LocalDateTime now = LocalDateTime.now();
        if (year < now.getYear()
                || year == now.getYear() && month < now.getMonthValue()) {
            throw new PaymentException("Card has expired");
        }
    }
}
