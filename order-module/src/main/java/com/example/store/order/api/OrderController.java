package com.example.store.order.api;

import com.example.store.order.dto.OrderRequest;
import com.example.store.order.dto.OrderResponse;
import com.example.store.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Orders", description = "Создание и управление заказами")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Создать заказ с оплатой")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заказ создан",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Не удалось создать заказ (например, отказ оплаты)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("REST request to create order for user: {}", request.getUserId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить заказ по идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найден",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Идентификатор заказа") @PathVariable UUID id) {
        log.info("REST request to get order by id: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Отменить заказ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказ отменён"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Идентификатор заказа") @PathVariable UUID id,
            @Parameter(description = "Причина отмены") @RequestParam String reason) {
        log.info("REST request to cancel order {}: {}", id, reason);
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok().build();
    }
}
