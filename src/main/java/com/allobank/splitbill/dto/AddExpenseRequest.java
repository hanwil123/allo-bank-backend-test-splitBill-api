package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.ExpenseCategory;
import com.allobank.splitbill.model.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AddExpenseRequest(
        @NotNull(message = "paidBy is required") UUID paidBy,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero") BigDecimal amount,
        @NotBlank(message = "description is required") String description,
        ExpenseCategory category,
        @NotNull(message = "splitType is required") SplitType splitType,

        List<ShareInput> splitAmong
) {
    public record ShareInput(
        @NotNull UUID participantId,
        BigDecimal amount,
        BigDecimal percentage
    ) {
    }
}
