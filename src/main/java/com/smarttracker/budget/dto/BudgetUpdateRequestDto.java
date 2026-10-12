package com.smarttracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BudgetUpdateRequestDto {

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Budget amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    public BudgetUpdateRequestDto() {
    }

    public BudgetUpdateRequestDto(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
