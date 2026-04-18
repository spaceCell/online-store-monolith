package com.example.store.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Данные банковской карты для оплаты")
@Data
public class PaymentInfo {

    @Schema(
            description = "Номер карты (мин. 16 цифр; оканчивается на 0000 — успех, 9999 — отказ)",
            example = "4111111111110000")
    private String cardNumber;

    @Schema(example = "IVAN IVANOV")
    private String cardHolderName;

    @Schema(description = "Срок действия MM/YY", example = "12/28")
    private String expiryDate;

    @Schema(example = "123")
    private String cvv;
}
