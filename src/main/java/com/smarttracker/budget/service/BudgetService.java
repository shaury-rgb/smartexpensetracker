package com.smarttracker.budget.service;

import com.smarttracker.budget.dto.BudgetRequestDto;
import com.smarttracker.budget.dto.BudgetResponseDto;
import com.smarttracker.budget.dto.BudgetUpdateRequestDto;
import com.smarttracker.budget.entity.Budget;
import com.smarttracker.budget.repository.BudgetRepository;
import com.smarttracker.category.entity.Category;
import com.smarttracker.category.repository.CategoryRepository;
import com.smarttracker.common.exception.ResourceNotFoundException;
import com.smarttracker.expense.repository.ExpenseRepository;
import com.smarttracker.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    public BudgetResponseDto createBudget(User user, BudgetRequestDto request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findAvailableCategoryById(request.getCategoryId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

            if (budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(user.getId(), request.getMonth(), request.getYear(), category.getId())) {
                throw new IllegalArgumentException("A budget for category '" + category.getName() + "' in " + request.getMonth() + "/" + request.getYear() + " already exists");
            }
        } else {
            if (budgetRepository.existsByUserIdAndMonthAndYearAndCategoryIsNull(user.getId(), request.getMonth(), request.getYear())) {
                throw new IllegalArgumentException("An overall budget for " + request.getMonth() + "/" + request.getYear() + " already exists");
            }
        }

        Budget budget = new Budget(user, category, request.getAmount(), request.getMonth(), request.getYear());
        Budget saved = budgetRepository.save(budget);

        return toBudgetResponse(saved, user.getId());
    }

    @Transactional(readOnly = true)
    public List<BudgetResponseDto> getBudgets(Long userId, int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 2000) {
            throw new IllegalArgumentException("Year must be 2000 or greater");
        }

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return budgets.stream()
                .map(budget -> toBudgetResponse(budget, userId))
                .toList();
    }

    public BudgetResponseDto updateBudget(Long id, Long userId, BudgetUpdateRequestDto request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));

        budget.setAmount(request.getAmount());
        Budget updated = budgetRepository.save(budget);

        return toBudgetResponse(updated, userId);
    }

    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));

        budgetRepository.delete(budget);
    }

    private BudgetResponseDto toBudgetResponse(Budget budget, Long userId) {
        BigDecimal actualSpending;
        if (budget.getCategory() == null) {
            actualSpending = expenseRepository.calculateMonthlySpending(userId, budget.getMonth(), budget.getYear());
        } else {
            actualSpending = expenseRepository.calculateCategoryMonthlySpending(
                    userId, budget.getCategory().getName(), budget.getMonth(), budget.getYear());
        }

        if (actualSpending == null) {
            actualSpending = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = budget.getAmount().subtract(actualSpending);
        BigDecimal percentageUsed;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            percentageUsed = BigDecimal.ZERO;
        } else {
            percentageUsed = actualSpending.multiply(BigDecimal.valueOf(100))
                    .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);
        }

        return BudgetResponseDto.of(budget, actualSpending, remainingAmount, percentageUsed);
    }
}
