package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public record CategoryBreakdownDto(
        String categoryName,
        BigDecimal totalSpending,
        long transactionCount,
        BigDecimal percentage
) {}
