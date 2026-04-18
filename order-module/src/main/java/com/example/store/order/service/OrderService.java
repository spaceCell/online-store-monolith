package com.example.store.order.service;

import com.example.store.catalog.domain.Product;
import com.example.store.catalog.service.CatalogService;
import com.example.store.order.domain.Order;
import com.example.store.order.domain.OrderItem;
import com.example.store.order.dto.OrderRequest;
import com.example.store.order.dto.OrderResponse;
import com.example.store.order.exception.OrderNotFoundException;
import com.example.store.order.repository.OrderRepository;
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
public class OrderService {

    private final CatalogService catalogService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    /**
     * Создание заказа с оплатой
     */
    public Order createOrder(OrderRequest request) {
        log.info("Создание заказа для пользователя {}", request.getUserId());

        // 1. Проверка пользователя
        User user = userService.validateUser(request.getUserId());

        // 2. Создаем позиции заказа
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Резервируем товар
            Product product = catalogService.reserveProduct(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity()
            );

            // Создаем Item
            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    itemRequest.getQuantity(),
                    product.getPrice()
            );

            orderItems.add(orderItem);
        }

        // 3. Создаем и сохраняем заказ
        Order order = new Order(user.getId(), orderItems);
        order = orderRepository.save(order);
        log.info("Заказ {} создан, сумма: {}", order.getId(), order.getTotalAmount());

        // 4. Обрабатываем платеж
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

            // Если платеж успешен - подтверждаем заказ
            if ("PAID".equals(paymentResponse.getStatus())) {
                order.confirm();
                order = orderRepository.save(order);
                log.info("Заказ {} подтвержден, транзакция: {}",
                        order.getId(), paymentResponse.getTransactionId());
            }

        } catch (PaymentException e) {
            // Платеж не прошел - отменяем заказ и возвращаем товары
            log.error("Ошибка оплаты для заказа {}: {}", order.getId(), e.getMessage());

            order.cancel();
            orderRepository.save(order);

            // Возвращаем товары на склад
            for (OrderItem item : order.getItems()) {
                catalogService.releaseProduct(item.getProductId(), item.getQuantity());
            }

            throw new RuntimeException("Ошибка создания заказа: " + e.getMessage());
        }

        return order;
    }

    /**
     * Получение заказа по ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + id));

        OrderResponse response = OrderResponse.fromEntity(order);

        // Добавляем информацию о платеже
        try {
            PaymentResponse payment = paymentService.getPaymentByOrderId(id);
            response.setPaymentStatus(payment.getStatus());
            response.setTransactionId(payment.getTransactionId());
        } catch (Exception e) {
            log.warn("Не удалось получить информацию о платеже для заказа {}", id);
        }

        return response;
    }

    /**
     * Получение платежа по ID заказа (для контроллера)
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    /**
     * Отмена заказа
     */
    public void cancelOrder(UUID orderId, String reason) {
        log.info("Отмена заказа {}: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + orderId));

        order.cancel();
        orderRepository.save(order);

        // Возвращаем товары на склад
        for (OrderItem item : order.getItems()) {
            catalogService.releaseProduct(item.getProductId(), item.getQuantity());
        }

        log.info("Заказ {} отменен", orderId);
    }

    /**
     * Подтверждение заказа
     */
    public void confirmOrder(UUID orderId) {
        log.info("Подтверждение заказа {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + orderId));

        order.confirm();
        orderRepository.save(order);

        log.info("Заказ {} подтвержден", orderId);
    }
}