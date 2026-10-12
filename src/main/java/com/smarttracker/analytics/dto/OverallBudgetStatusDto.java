package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public record OverallBudgetStatusDto(
        boolean configured,
        BigDecimal budgetAmount,
        BigDecimal actualSpending,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        boolean exceeded
) {}
