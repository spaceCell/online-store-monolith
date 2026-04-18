package com.example.store.order.mapper;

import com.example.store.order.domain.Order;
import com.example.store.order.domain.OrderItem;
import com.example.store.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "status", expression = "java(order.getStatus() == null ? null : order.getStatus().name())")
    OrderResponse toResponse(Order order);

    OrderResponse.OrderItemResponse toItemResponse(OrderItem item);
}
