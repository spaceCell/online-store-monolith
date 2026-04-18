package com.example.store.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание заказа и оплату")
@Data
public class OrderRequest {

    @Schema(description = "Идентификатор пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;

    @Schema(description = "Позиции заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;

    @Schema(description = "Данные карты", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentInfo paymentInfo;
}
