package com.example.store.payment.service.support;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TransactionIdGenerator {

    private static final int RANDOM_PART_LENGTH = 8;
    private static final DateTimeFormatter DATE_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, RANDOM_PART_LENGTH).toUpperCase()
                + "-" + LocalDateTime.now().format(DATE_SUFFIX);
    }
}
