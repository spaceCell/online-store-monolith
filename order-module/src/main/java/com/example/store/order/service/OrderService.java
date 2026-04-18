package com.example.store.order.service;

import com.example.store.order.dto.OrderRequest;
import com.example.store.order.dto.OrderResponse;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(UUID id);

    void cancelOrder(UUID orderId, String reason);
}
