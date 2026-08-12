package com.allobank.splitbill.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceChargeCalculatorTest {

    @Test
    void matchesWorkedExampleFromAssignment() {
        assertEquals(0, ServiceChargeCalculator.calculatePct("hanhan123"));
    }

    @Test
    void isCaseInsensitive() {
        int lower = ServiceChargeCalculator.calculatePct("hanhan123");
        int upper = ServiceChargeCalculator.calculatePct("HANHAN123");
        assertEquals(lower, upper);
    }

    @Test
    void calculateAmountAppliesPercentageWithTwoDecimalRounding() {
        ServiceChargeCalculator calculator = new ServiceChargeCalculator("testtest");
        BigDecimal amount = calculator.calculateAmount(new BigDecimal("123.45"));
        assertEquals(0, new BigDecimal("6.17").compareTo(amount));
    }
}
