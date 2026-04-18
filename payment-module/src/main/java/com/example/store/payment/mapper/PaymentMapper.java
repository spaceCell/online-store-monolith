package com.example.store.payment.mapper;

import com.example.store.payment.domain.Payment;
import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", expression = "java(payment.getStatus() == null ? null : payment.getStatus().name())")
    PaymentResponse toResponse(Payment payment);

    default Payment toEntity(PaymentRequest request) {
        return new Payment(request.getOrderId(), request.getUserId(), request.getAmount());
    }
}
