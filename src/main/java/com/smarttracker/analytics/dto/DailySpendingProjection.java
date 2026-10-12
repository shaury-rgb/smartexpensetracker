package com.smarttracker.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySpendingProjection {
    LocalDate getExpenseDate();
    BigDecimal getTotalSpending();
    Long getTransactionCount();
}
