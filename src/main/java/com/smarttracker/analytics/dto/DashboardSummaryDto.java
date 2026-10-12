package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        int month,
        int year,
        BigDecimal totalSpending,
        long expenseCount,
        BigDecimal averageExpenseAmount,
        BigDecimal highestExpenseAmount,
        BigDecimal lowestExpenseAmount,
        BigDecimal previousMonthTotalSpending,
        BigDecimal absoluteChange,
        BigDecimal percentageChange,
        BigDecimal budgetAmount,
        BigDecimal remainingBudget
) {}
