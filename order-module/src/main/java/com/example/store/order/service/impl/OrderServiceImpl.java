package com.example.store.order.service.impl;

import com.example.store.catalog.domain.Product;
import com.example.store.catalog.service.CatalogService;
import com.example.store.order.domain.Order;
import com.example.store.order.domain.OrderItem;
import com.example.store.order.dto.OrderItemRequest;
import com.example.store.order.dto.OrderRequest;
import com.example.store.order.dto.OrderResponse;
import com.example.store.order.exception.OrderNotFoundException;
import com.example.store.order.exception.OrderProcessingException;
import com.example.store.order.mapper.OrderMapper;
import com.example.store.order.repository.OrderRepository;
import com.example.store.order.service.OrderService;
import com.example.store.payment.dto.PaymentRequest;
import com.example.store.payment.dto.PaymentResponse;
import com.example.store.payment.exception.PaymentException;
import com.example.store.payment.service.PaymentService;
import com.example.store.user.domain.User;
import com.example.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Заказ не найден: ";

    private final CatalogService catalogService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Создание заказа для пользователя {}", request.getUserId());

        User user = userService.validateUser(request.getUserId());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = catalogService.reserveProduct(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity()
            );

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    itemRequest.getQuantity(),
                    product.getPrice()
            );

            orderItems.add(orderItem);
        }

        Order order = new Order(user.getId(), orderItems);
        order = orderRepository.save(order);
        log.info("Заказ {} создан, сумма: {}", order.getId(), order.getTotalAmount());

        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(order.getId());
            paymentRequest.setUserId(user.getId());
            paymentRequest.setAmount(order.getTotalAmount());
            paymentRequest.setCardNumber(request.getPaymentInfo().getCardNumber());
            paymentRequest.setCardHolderName(request.getPaymentInfo().getCardHolderName());
            paymentRequest.setExpiryDate(request.getPaymentInfo().getExpiryDate());
            paymentRequest.setCvv(request.getPaymentInfo().getCvv());

            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

            if ("PAID".equals(paymentResponse.getStatus())) {
                order.confirm();
                order = orderRepository.save(order);
                log.info("Заказ {} подтвержден, транзакция: {}",
                        order.getId(), paymentResponse.getTransactionId());
            }

        } catch (PaymentException e) {
            log.error("Ошибка оплаты для заказа {}: {}", order.getId(), e.getMessage());

            order.cancel();
            orderRepository.save(order);

            for (OrderItem item : order.getItems()) {
                catalogService.releaseProduct(item.getProductId(), item.getQuantity());
            }

            throw new OrderProcessingException("Ошибка создания заказа: " + e.getMessage(), e);
        }

        OrderResponse response = orderMapper.toResponse(order);
        attachPaymentInfo(response, order.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MSG + id));

        OrderResponse response = orderMapper.toResponse(order);
        attachPaymentInfo(response, id);
        return response;
    }

    @Override
    public void cancelOrder(UUID orderId, String reason) {
        log.info("Отмена заказа {}: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MSG + orderId));

        order.cancel();
        orderRepository.save(order);

        for (OrderItem item : order.getItems()) {
            catalogService.releaseProduct(item.getProductId(), item.getQuantity());
        }

        log.info("Заказ {} отменен", orderId);
    }

    private void attachPaymentInfo(OrderResponse response, UUID orderId) {
        try {
            PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
            response.setPaymentStatus(payment.getStatus());
            response.setTransactionId(payment.getTransactionId());
        } catch (PaymentException e) {
            log.warn("Не удалось получить информацию о платеже для заказа {}", orderId);
        }
    }
}
