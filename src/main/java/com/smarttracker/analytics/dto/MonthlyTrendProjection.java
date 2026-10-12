package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public interface MonthlyTrendProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotalSpending();
    Long getTransactionCount();
}
