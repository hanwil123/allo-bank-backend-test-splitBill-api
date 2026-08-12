package com.allobank.splitbill.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ServiceChargeCalculator {

    private final String githubUsername;

    public ServiceChargeCalculator(@Value("${app.github-username}") String githubUsername) {
        if (githubUsername == null || githubUsername.isBlank()) {
            throw new IllegalStateException("app.github-username is not configured. Set it in application.properties.");
        }
        this.githubUsername = githubUsername;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public int calculatePct() {
        return calculatePct(githubUsername);
    }

    public static int calculatePct(String username) {
        String lower = username.toLowerCase();
        long sum = 0;
        for (int i = 0; i < lower.length(); i++) {
            sum += lower.charAt(i);
        }
        return (int) (sum % 10);
    }

    public BigDecimal calculateAmount(BigDecimal total) {
        BigDecimal pct = BigDecimal.valueOf(calculatePct());
        return total.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
