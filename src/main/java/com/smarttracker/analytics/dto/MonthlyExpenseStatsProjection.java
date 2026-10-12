package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public interface MonthlyExpenseStatsProjection {
    BigDecimal getTotalSpending();
    Long getExpenseCount();
    BigDecimal getAverageAmount();
    BigDecimal getMaxAmount();
    BigDecimal getMinAmount();
}
