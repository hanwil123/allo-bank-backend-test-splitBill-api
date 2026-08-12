package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuditEventResponse(
        String type,
        String summary,
        BigDecimal amount,
        Instant occurredAt
) {
}
