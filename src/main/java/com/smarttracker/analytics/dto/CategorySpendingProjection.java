package com.smarttracker.analytics.dto;

import java.math.BigDecimal;

public interface CategorySpendingProjection {
    String getCategoryName();
    BigDecimal getTotalSpending();
    Long getTransactionCount();
}
