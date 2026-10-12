package com.smarttracker.analytics.dto;

import java.util.List;

public record BudgetStatusAnalyticsDto(
        int month,
        int year,
        OverallBudgetStatusDto overallBudget,
        List<CategoryBudgetStatusDto> categoryBudgets
) {}
