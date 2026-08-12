package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseCategory;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.SplitType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ExpenseResponse(
        UUID id,
        UUID paidBy,
        String paidByName,
        BigDecimal amount,
        String description,
        ExpenseCategory category,
        SplitType splitType,
        List<ShareDto> shares,
        Instant createdAt
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getCategory(),
                expense.getSplitType(),
                expense.getShares().stream().map(ShareDto::from).collect(Collectors.toList()),
                expense.getCreatedAt()
        );
    }

    public record ShareDto(UUID participantId, String participantName, BigDecimal amount) {
        public static ShareDto from(ExpenseShare share) {
            return new ShareDto(
                    share.getParticipant().getId(),
                    share.getParticipant().getName(),
                    share.getAmount()
            );
        }
    }
}
