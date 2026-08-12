package com.allobank.splitbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull UUID from,
        @NotNull UUID to,
        @NotNull @DecimalMin(value = "0.01", message = "amount must be greater than zero") BigDecimal amount
) {
}
