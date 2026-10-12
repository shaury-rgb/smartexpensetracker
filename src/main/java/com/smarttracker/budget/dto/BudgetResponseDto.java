package com.smarttracker.budget.dto;

import com.smarttracker.budget.entity.Budget;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BudgetResponseDto(
        Long id,
        int month,
        int year,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal actualSpending,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static BudgetResponseDto of(
            Budget budget,
            BigDecimal actualSpending,
            BigDecimal remainingAmount,
            BigDecimal percentageUsed
    ) {
        return new BudgetResponseDto(
                budget.getId(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getCategory() != null ? budget.getCategory().getName() : null,
                budget.getAmount(),
                actualSpending,
                remainingAmount,
                percentageUsed,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
