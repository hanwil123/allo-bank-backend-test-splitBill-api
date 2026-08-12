package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID fromId,
        String fromName,
        UUID toId,
        String toName,
        BigDecimal amount,
        Instant paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getFrom().getId(),
                payment.getFrom().getName(),
                payment.getTo().getId(),
                payment.getTo().getName(),
                payment.getAmount(),
                payment.getPaidAt()
        );
    }
}
