package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public record CategoryBudgetStatusDto(
        Long categoryId,
        String categoryName,
        BigDecimal budgetAmount,
        BigDecimal actualSpending,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        boolean exceeded
) {}
