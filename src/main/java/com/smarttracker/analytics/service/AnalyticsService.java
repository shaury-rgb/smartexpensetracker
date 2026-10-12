package com.smarttracker.analytics.service;

import com.smarttracker.analytics.dto.BudgetStatusAnalyticsDto;
import com.smarttracker.analytics.dto.CategoryBreakdownDto;
import com.smarttracker.analytics.dto.CategoryBudgetStatusDto;
import com.smarttracker.analytics.dto.CategorySpendingProjection;
import com.smarttracker.analytics.dto.DailySpendingProjection;
import com.smarttracker.analytics.dto.DailyTrendDto;
import com.smarttracker.analytics.dto.DashboardSummaryDto;
import com.smarttracker.analytics.dto.MonthlyExpenseStatsProjection;
import com.smarttracker.analytics.dto.MonthlyTrendDto;
import com.smarttracker.analytics.dto.MonthlyTrendProjection;
import com.smarttracker.analytics.dto.OverallBudgetStatusDto;
import com.smarttracker.budget.entity.Budget;
import com.smarttracker.budget.repository.BudgetRepository;
import com.smarttracker.category.entity.Category;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.expense.entity.Expense;
import com.smarttracker.expense.repository.ExpenseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public AnalyticsService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public DashboardSummaryDto getDashboardSummary(Long userId, int month, int year) {
        validateMonthAndYear(month, year);

        YearMonth currentYm = YearMonth.of(year, month);
        LocalDate startDate = currentYm.atDay(1);
        LocalDate endDate = currentYm.atEndOfMonth();

        MonthlyExpenseStatsProjection stats = expenseRepository.getMonthlyStats(userId, startDate, endDate);

        BigDecimal totalSpending = (stats != null && stats.getTotalSpending() != null)
                ? stats.getTotalSpending().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        long expenseCount = (stats != null && stats.getExpenseCount() != null)
                ? stats.getExpenseCount()
                : 0L;
        BigDecimal avgAmount = (stats != null && stats.getAverageAmount() != null)
                ? stats.getAverageAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxAmount = (stats != null && stats.getMaxAmount() != null && expenseCount > 0)
                ? stats.getMaxAmount().setScale(2, RoundingMode.HALF_UP)
                : null;
        BigDecimal minAmount = (stats != null && stats.getMinAmount() != null && expenseCount > 0)
                ? stats.getMinAmount().setScale(2, RoundingMode.HALF_UP)
                : null;

        YearMonth prevYm = currentYm.minusMonths(1);
        BigDecimal prevSpending = expenseRepository.calculateMonthlySpending(
                userId, prevYm.getMonthValue(), prevYm.getYear());
        if (prevSpending == null) {
            prevSpending = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            prevSpending = prevSpending.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal absoluteChange = totalSpending.subtract(prevSpending).setScale(2, RoundingMode.HALF_UP);

        BigDecimal percentageChange;
        if (prevSpending.compareTo(BigDecimal.ZERO) == 0) {
            if (totalSpending.compareTo(BigDecimal.ZERO) == 0) {
                percentageChange = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                percentageChange = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
            }
        } else {
            percentageChange = absoluteChange.multiply(BigDecimal.valueOf(100))
                    .divide(prevSpending, 2, RoundingMode.HALF_UP);
        }

        Optional<Budget> overallBudgetOpt = budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(userId, month, year);
        BigDecimal budgetAmount = null;
        BigDecimal remainingBudget = null;
        if (overallBudgetOpt.isPresent()) {
            budgetAmount = overallBudgetOpt.get().getAmount().setScale(2, RoundingMode.HALF_UP);
            remainingBudget = budgetAmount.subtract(totalSpending).setScale(2, RoundingMode.HALF_UP);
        }

        return new DashboardSummaryDto(
                month,
                year,
                totalSpending,
                expenseCount,
                avgAmount,
                maxAmount,
                minAmount,
                prevSpending,
                absoluteChange,
                percentageChange,
                budgetAmount,
                remainingBudget
        );
    }

    public List<CategoryBreakdownDto> getCategoryBreakdown(Long userId, int month, int year) {
        validateMonthAndYear(month, year);

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<CategorySpendingProjection> projections = expenseRepository.findCategorySpending(userId, startDate, endDate);

        BigDecimal monthTotal = projections.stream()
                .map(CategorySpendingProjection::getTotalSpending)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return projections.stream()
                .filter(p -> p.getTotalSpending() != null && p.getTotalSpending().compareTo(BigDecimal.ZERO) > 0)
                .map(p -> {
                    BigDecimal spending = p.getTotalSpending().setScale(2, RoundingMode.HALF_UP);
                    long count = p.getTransactionCount() != null ? p.getTransactionCount() : 0L;
                    BigDecimal percentage;
                    if (monthTotal.compareTo(BigDecimal.ZERO) <= 0) {
                        percentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    } else {
                        percentage = spending.multiply(BigDecimal.valueOf(100))
                                .divide(monthTotal, 2, RoundingMode.HALF_UP);
                    }
                    return new CategoryBreakdownDto(p.getCategoryName(), spending, count, percentage);
                })
                .toList();
    }

    public List<MonthlyTrendDto> getMonthlyTrend(Long userId, int months, Integer endMonth, Integer endYear) {
        if (months < 1 || months > 24) {
            throw new IllegalArgumentException("Months must be between 1 and 24");
        }

        YearMonth endYm;
        if (endMonth != null && endYear != null) {
            validateMonthAndYear(endMonth, endYear);
            endYm = YearMonth.of(endYear, endMonth);
        } else {
            endYm = YearMonth.now();
        }

        YearMonth startYm = endYm.minusMonths(months - 1);
        LocalDate startDate = startYm.atDay(1);
        LocalDate endDate = endYm.atEndOfMonth();

        List<MonthlyTrendProjection> projections = expenseRepository.findMonthlySpendingTrend(userId, startDate, endDate);
        Map<YearMonth, MonthlyTrendProjection> map = projections.stream()
                .collect(Collectors.toMap(
                        p -> YearMonth.of(p.getYear(), p.getMonth()),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<MonthlyTrendDto> result = new ArrayList<>();
        YearMonth current = startYm;
        while (!current.isAfter(endYm)) {
            MonthlyTrendProjection proj = map.get(current);
            BigDecimal spending = (proj != null && proj.getTotalSpending() != null)
                    ? proj.getTotalSpending().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            long count = (proj != null && proj.getTransactionCount() != null)
                    ? proj.getTransactionCount()
                    : 0L;
            String label = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase() + " " + current.getYear();
            result.add(new MonthlyTrendDto(current.getYear(), current.getMonthValue(), label, spending, count));
            current = current.plusMonths(1);
        }

        return result;
    }

    public List<DailyTrendDto> getDailyTrend(Long userId, int month, int year) {
        validateMonthAndYear(month, year);

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<DailySpendingProjection> projections = expenseRepository.findDailySpending(userId, startDate, endDate);
        Map<LocalDate, DailySpendingProjection> map = projections.stream()
                .collect(Collectors.toMap(
                        DailySpendingProjection::getExpenseDate,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<DailyTrendDto> result = new ArrayList<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            DailySpendingProjection proj = map.get(date);
            BigDecimal spending = (proj != null && proj.getTotalSpending() != null)
                    ? proj.getTotalSpending().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            long count = (proj != null && proj.getTransactionCount() != null)
                    ? proj.getTransactionCount()
                    : 0L;
            result.add(new DailyTrendDto(date, day, spending, count));
        }

        return result;
    }

    public List<ExpenseResponseDto> getRecentExpenses(Long userId, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        List<Expense> expenses = expenseRepository.findRecentExpenses(userId, PageRequest.of(0, limit));
        return expenses.stream().map(ExpenseResponseDto::fromEntity).toList();
    }

    public List<ExpenseResponseDto> getTopExpenses(Long userId, int month, int year, int limit) {
        validateMonthAndYear(month, year);
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findTopExpenses(userId, startDate, endDate, PageRequest.of(0, limit));
        return expenses.stream().map(ExpenseResponseDto::fromEntity).toList();
    }

    public BudgetStatusAnalyticsDto getBudgetStatusAnalytics(Long userId, int month, int year) {
        validateMonthAndYear(month, year);

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        BigDecimal actualTotal = expenseRepository.calculateMonthlySpending(userId, month, year);
        if (actualTotal == null) {
            actualTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            actualTotal = actualTotal.setScale(2, RoundingMode.HALF_UP);
        }

        Budget overallBudget = budgets.stream()
                .filter(b -> b.getCategory() == null)
                .findFirst()
                .orElse(null);

        OverallBudgetStatusDto overallStatus;
        if (overallBudget != null) {
            BigDecimal budgetAmount = overallBudget.getAmount().setScale(2, RoundingMode.HALF_UP);
            BigDecimal remaining = budgetAmount.subtract(actualTotal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal percentageUsed = budgetAmount.compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : actualTotal.multiply(BigDecimal.valueOf(100)).divide(budgetAmount, 2, RoundingMode.HALF_UP);
            boolean exceeded = actualTotal.compareTo(budgetAmount) > 0;
            overallStatus = new OverallBudgetStatusDto(true, budgetAmount, actualTotal, remaining, percentageUsed, exceeded);
        } else {
            overallStatus = new OverallBudgetStatusDto(false, null, actualTotal, null, null, false);
        }

        List<CategoryBudgetStatusDto> categoryBudgetStatuses = budgets.stream()
                .filter(b -> b.getCategory() != null)
                .map(b -> {
                    Category cat = b.getCategory();
                    BigDecimal budgetAmount = b.getAmount().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal catSpending = expenseRepository.calculateCategoryMonthlySpending(
                            userId, cat.getName(), month, year);
                    if (catSpending == null) {
                        catSpending = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    } else {
                        catSpending = catSpending.setScale(2, RoundingMode.HALF_UP);
                    }

                    BigDecimal remaining = budgetAmount.subtract(catSpending).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal percentageUsed = budgetAmount.compareTo(BigDecimal.ZERO) <= 0
                            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            : catSpending.multiply(BigDecimal.valueOf(100)).divide(budgetAmount, 2, RoundingMode.HALF_UP);
                    boolean exceeded = catSpending.compareTo(budgetAmount) > 0;

                    return new CategoryBudgetStatusDto(
                            cat.getId(),
                            cat.getName(),
                            budgetAmount,
                            catSpending,
                            remaining,
                            percentageUsed,
                            exceeded
                    );
                })
                .toList();

        return new BudgetStatusAnalyticsDto(month, year, overallStatus, categoryBudgetStatuses);
    }

    private void validateMonthAndYear(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }
    }
}
