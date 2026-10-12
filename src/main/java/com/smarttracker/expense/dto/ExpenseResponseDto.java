package com.smarttracker.expense.dto;

import com.smarttracker.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExpenseResponseDto(
        Long id,
        String title,
        BigDecimal amount,
        String category,
        String description,
        LocalDate expenseDate,
        String paymentMethod,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ExpenseResponseDto fromEntity(Expense expense) {
        if (expense == null) {
            return null;
        }
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getPaymentMethod(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
