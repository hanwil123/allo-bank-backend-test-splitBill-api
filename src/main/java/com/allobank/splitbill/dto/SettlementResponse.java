package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID groupId,
        String groupName,
        BigDecimal totalExpenses,
        List<BalanceDto> balances,
        List<TransactionDto> transactions,
        int service_charge_pct,
        BigDecimal service_charge_amount
) {
    public record BalanceDto(UUID participantId, String participantName, BigDecimal netBalance) {
    }

    public record TransactionDto(UUID fromId, String fromName, UUID toId, String toName, BigDecimal amount) {
    }
}
