package com.example.store.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Schema(description = "Позиция заказа: товар и количество")
@Data
public class OrderItemRequest {

    @Schema(description = "Идентификатор товара", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID productId;

    @Schema(description = "Количество", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
