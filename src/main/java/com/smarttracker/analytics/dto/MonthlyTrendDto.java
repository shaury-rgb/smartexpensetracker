package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public record MonthlyTrendDto(
        int year,
        int month,
        String monthLabel,
        BigDecimal totalSpending,
        long transactionCount
) {}
