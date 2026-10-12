package com.smarttracker.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTrendDto(
        LocalDate date,
        int day,
        BigDecimal totalSpending,
        long transactionCount
) {}
