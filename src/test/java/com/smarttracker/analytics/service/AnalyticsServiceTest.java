package com.smarttracker.analytics.service;

import com.smarttracker.analytics.dto.BudgetStatusAnalyticsDto;
import com.smarttracker.analytics.dto.CategoryBreakdownDto;
import com.smarttracker.analytics.dto.CategorySpendingProjection;
import com.smarttracker.analytics.dto.DailySpendingProjection;
import com.smarttracker.analytics.dto.DailyTrendDto;
import com.smarttracker.analytics.dto.DashboardSummaryDto;
import com.smarttracker.analytics.dto.MonthlyExpenseStatsProjection;
import com.smarttracker.analytics.dto.MonthlyTrendDto;
import com.smarttracker.analytics.dto.MonthlyTrendProjection;
import com.smarttracker.budget.entity.Budget;
import com.smarttracker.budget.repository.BudgetRepository;
import com.smarttracker.category.entity.Category;
import com.smarttracker.expense.entity.Expense;
import com.smarttracker.expense.repository.ExpenseRepository;
import com.smarttracker.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Alice", "alice@test.com", "hash");
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Dashboard summary: Empty dataset returns 0.00 spending, 0 count, null max/min, 0% change")
    void testDashboardSummaryEmptyDataset() {
        MonthlyExpenseStatsProjection emptyStats = mock(MonthlyExpenseStatsProjection.class);
        when(emptyStats.getTotalSpending()).thenReturn(BigDecimal.ZERO);
        when(emptyStats.getExpenseCount()).thenReturn(0L);
        when(emptyStats.getAverageAmount()).thenReturn(BigDecimal.ZERO);
        when(emptyStats.getMaxAmount()).thenReturn(null);
        when(emptyStats.getMinAmount()).thenReturn(null);

        when(expenseRepository.getMonthlyStats(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(emptyStats);
        when(expenseRepository.calculateMonthlySpending(1L, 9, 2026))
                .thenReturn(BigDecimal.ZERO);
        when(budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(1L, 10, 2026))
                .thenReturn(Optional.empty());

        DashboardSummaryDto summary = analyticsService.getDashboardSummary(1L, 10, 2026);

        assertThat(summary.month()).isEqualTo(10);
        assertThat(summary.year()).isEqualTo(2026);
        assertThat(summary.totalSpending()).isEqualByComparingTo("0.00");
        assertThat(summary.expenseCount()).isEqualTo(0L);
        assertThat(summary.averageExpenseAmount()).isEqualByComparingTo("0.00");
        assertThat(summary.highestExpenseAmount()).isNull();
        assertThat(summary.lowestExpenseAmount()).isNull();
        assertThat(summary.previousMonthTotalSpending()).isEqualByComparingTo("0.00");
        assertThat(summary.absoluteChange()).isEqualByComparingTo("0.00");
        assertThat(summary.percentageChange()).isEqualByComparingTo("0.00");
        assertThat(summary.budgetAmount()).isNull();
        assertThat(summary.remainingBudget()).isNull();
    }

    @Test
    @DisplayName("Dashboard summary: Current spending with positive previous spending")
    void testDashboardSummaryWithPreviousMonthSpending() {
        MonthlyExpenseStatsProjection stats = mock(MonthlyExpenseStatsProjection.class);
        when(stats.getTotalSpending()).thenReturn(new BigDecimal("150.00"));
        when(stats.getExpenseCount()).thenReturn(3L);
        when(stats.getAverageAmount()).thenReturn(new BigDecimal("50.00"));
        when(stats.getMaxAmount()).thenReturn(new BigDecimal("80.00"));
        when(stats.getMinAmount()).thenReturn(new BigDecimal("20.00"));

        when(expenseRepository.getMonthlyStats(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(stats);
        when(expenseRepository.calculateMonthlySpending(1L, 9, 2026))
                .thenReturn(new BigDecimal("100.00"));

        Budget overallBudget = new Budget(testUser, null, new BigDecimal("200.00"), 10, 2026);
        when(budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(1L, 10, 2026))
                .thenReturn(Optional.of(overallBudget));

        DashboardSummaryDto summary = analyticsService.getDashboardSummary(1L, 10, 2026);

        assertThat(summary.totalSpending()).isEqualByComparingTo("150.00");
        assertThat(summary.expenseCount()).isEqualTo(3L);
        assertThat(summary.averageExpenseAmount()).isEqualByComparingTo("50.00");
        assertThat(summary.highestExpenseAmount()).isEqualByComparingTo("80.00");
        assertThat(summary.lowestExpenseAmount()).isEqualByComparingTo("20.00");
        assertThat(summary.previousMonthTotalSpending()).isEqualByComparingTo("100.00");
        assertThat(summary.absoluteChange()).isEqualByComparingTo("50.00");
        assertThat(summary.percentageChange()).isEqualByComparingTo("50.00");
        assertThat(summary.budgetAmount()).isEqualByComparingTo("200.00");
        assertThat(summary.remainingBudget()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Dashboard summary: Zero previous month spending with positive current spending produces 100% change")
    void testDashboardSummaryZeroPreviousPositiveCurrent() {
        MonthlyExpenseStatsProjection stats = mock(MonthlyExpenseStatsProjection.class);
        when(stats.getTotalSpending()).thenReturn(new BigDecimal("100.00"));
        when(stats.getExpenseCount()).thenReturn(1L);
        when(stats.getAverageAmount()).thenReturn(new BigDecimal("100.00"));
        when(stats.getMaxAmount()).thenReturn(new BigDecimal("100.00"));
        when(stats.getMinAmount()).thenReturn(new BigDecimal("100.00"));

        when(expenseRepository.getMonthlyStats(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(stats);
        when(expenseRepository.calculateMonthlySpending(1L, 9, 2026))
                .thenReturn(BigDecimal.ZERO);
        when(budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(1L, 10, 2026))
                .thenReturn(Optional.empty());

        DashboardSummaryDto summary = analyticsService.getDashboardSummary(1L, 10, 2026);

        assertThat(summary.previousMonthTotalSpending()).isEqualByComparingTo("0.00");
        assertThat(summary.absoluteChange()).isEqualByComparingTo("100.00");
        assertThat(summary.percentageChange()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Category breakdown: Calculate percentages accurately and sort descending")
    void testCategoryBreakdown() {
        CategorySpendingProjection p1 = mock(CategorySpendingProjection.class);
        when(p1.getCategoryName()).thenReturn("Food");
        when(p1.getTotalSpending()).thenReturn(new BigDecimal("75.00"));
        when(p1.getTransactionCount()).thenReturn(3L);

        CategorySpendingProjection p2 = mock(CategorySpendingProjection.class);
        when(p2.getCategoryName()).thenReturn("Transport");
        when(p2.getTotalSpending()).thenReturn(new BigDecimal("25.00"));
        when(p2.getTransactionCount()).thenReturn(1L);

        when(expenseRepository.findCategorySpending(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(p1, p2));

        List<CategoryBreakdownDto> breakdown = analyticsService.getCategoryBreakdown(1L, 10, 2026);

        assertThat(breakdown).hasSize(2);
        assertThat(breakdown.get(0).categoryName()).isEqualTo("Food");
        assertThat(breakdown.get(0).totalSpending()).isEqualByComparingTo("75.00");
        assertThat(breakdown.get(0).transactionCount()).isEqualTo(3L);
        assertThat(breakdown.get(0).percentage()).isEqualByComparingTo("75.00");

        assertThat(breakdown.get(1).categoryName()).isEqualTo("Transport");
        assertThat(breakdown.get(1).totalSpending()).isEqualByComparingTo("25.00");
        assertThat(breakdown.get(1).transactionCount()).isEqualTo(1L);
        assertThat(breakdown.get(1).percentage()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("Monthly trend: Cross year boundary and include months with zero spending")
    void testMonthlyTrendCrossingYearBoundary() {
        // Query 4 months ending in Feb 2026 -> Nov 2025, Dec 2025, Jan 2026, Feb 2026
        MonthlyTrendProjection projDec = mock(MonthlyTrendProjection.class);
        when(projDec.getYear()).thenReturn(2025);
        when(projDec.getMonth()).thenReturn(12);
        when(projDec.getTotalSpending()).thenReturn(new BigDecimal("120.00"));
        when(projDec.getTransactionCount()).thenReturn(2L);

        when(expenseRepository.findMonthlySpendingTrend(eq(1L), eq(LocalDate.of(2025, 11, 1)), eq(LocalDate.of(2026, 2, 28))))
                .thenReturn(List.of(projDec));

        List<MonthlyTrendDto> trend = analyticsService.getMonthlyTrend(1L, 4, 2, 2026);

        assertThat(trend).hasSize(4);
        // Nov 2025: zero
        assertThat(trend.get(0).year()).isEqualTo(2025);
        assertThat(trend.get(0).month()).isEqualTo(11);
        assertThat(trend.get(0).monthLabel()).isEqualTo("NOV 2025");
        assertThat(trend.get(0).totalSpending()).isEqualByComparingTo("0.00");
        assertThat(trend.get(0).transactionCount()).isEqualTo(0L);

        // Dec 2025: 120.00
        assertThat(trend.get(1).year()).isEqualTo(2025);
        assertThat(trend.get(1).month()).isEqualTo(12);
        assertThat(trend.get(1).monthLabel()).isEqualTo("DEC 2025");
        assertThat(trend.get(1).totalSpending()).isEqualByComparingTo("120.00");
        assertThat(trend.get(1).transactionCount()).isEqualTo(2L);

        // Jan 2026: zero
        assertThat(trend.get(2).year()).isEqualTo(2026);
        assertThat(trend.get(2).month()).isEqualTo(1);
        assertThat(trend.get(2).monthLabel()).isEqualTo("JAN 2026");
        assertThat(trend.get(2).totalSpending()).isEqualByComparingTo("0.00");

        // Feb 2026: zero
        assertThat(trend.get(3).year()).isEqualTo(2026);
        assertThat(trend.get(3).month()).isEqualTo(2);
        assertThat(trend.get(3).monthLabel()).isEqualTo("FEB 2026");
        assertThat(trend.get(3).totalSpending()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Daily trend: Leap year February 2024 has 29 days with all missing days filled")
    void testDailyTrendLeapYear() {
        DailySpendingProjection leapDayProj = mock(DailySpendingProjection.class);
        when(leapDayProj.getExpenseDate()).thenReturn(LocalDate.of(2024, 2, 29));
        when(leapDayProj.getTotalSpending()).thenReturn(new BigDecimal("50.00"));
        when(leapDayProj.getTransactionCount()).thenReturn(1L);

        when(expenseRepository.findDailySpending(eq(1L), eq(LocalDate.of(2024, 2, 1)), eq(LocalDate.of(2024, 2, 29))))
                .thenReturn(List.of(leapDayProj));

        List<DailyTrendDto> dailyTrend = analyticsService.getDailyTrend(1L, 2, 2024);

        assertThat(dailyTrend).hasSize(29);
        // Day 1: 0.00
        assertThat(dailyTrend.get(0).day()).isEqualTo(1);
        assertThat(dailyTrend.get(0).totalSpending()).isEqualByComparingTo("0.00");
        assertThat(dailyTrend.get(0).transactionCount()).isEqualTo(0L);

        // Day 29: 50.00
        assertThat(dailyTrend.get(28).day()).isEqualTo(29);
        assertThat(dailyTrend.get(28).date()).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(dailyTrend.get(28).totalSpending()).isEqualByComparingTo("50.00");
        assertThat(dailyTrend.get(28).transactionCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Budget status analytics: Exceeded overall budget and category budgets")
    void testBudgetStatusAnalytics() {
        Budget overall = new Budget(testUser, null, new BigDecimal("100.00"), 10, 2026);
        Category foodCat = new Category("Food", true, null);
        foodCat.setId(5L);
        Budget catBudget = new Budget(testUser, foodCat, new BigDecimal("50.00"), 10, 2026);

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 10, 2026))
                .thenReturn(List.of(overall, catBudget));

        // Total spending 120.00 on 100.00 budget -> Exceeded!
        when(expenseRepository.calculateMonthlySpending(1L, 10, 2026))
                .thenReturn(new BigDecimal("120.00"));

        // Food spending 30.00 on 50.00 budget -> Remaining 20.00, 60% used, Not exceeded
        when(expenseRepository.calculateCategoryMonthlySpending(1L, "Food", 10, 2026))
                .thenReturn(new BigDecimal("30.00"));

        BudgetStatusAnalyticsDto status = analyticsService.getBudgetStatusAnalytics(1L, 10, 2026);

        assertThat(status.overallBudget().configured()).isTrue();
        assertThat(status.overallBudget().budgetAmount()).isEqualByComparingTo("100.00");
        assertThat(status.overallBudget().actualSpending()).isEqualByComparingTo("120.00");
        assertThat(status.overallBudget().remainingAmount()).isEqualByComparingTo("-20.00");
        assertThat(status.overallBudget().percentageUsed()).isEqualByComparingTo("120.00");
        assertThat(status.overallBudget().exceeded()).isTrue();

        assertThat(status.categoryBudgets()).hasSize(1);
        assertThat(status.categoryBudgets().get(0).categoryId()).isEqualTo(5L);
        assertThat(status.categoryBudgets().get(0).categoryName()).isEqualTo("Food");
        assertThat(status.categoryBudgets().get(0).budgetAmount()).isEqualByComparingTo("50.00");
        assertThat(status.categoryBudgets().get(0).actualSpending()).isEqualByComparingTo("30.00");
        assertThat(status.categoryBudgets().get(0).remainingAmount()).isEqualByComparingTo("20.00");
        assertThat(status.categoryBudgets().get(0).percentageUsed()).isEqualByComparingTo("60.00");
        assertThat(status.categoryBudgets().get(0).exceeded()).isFalse();
    }

    @Test
    @DisplayName("Parameter validation: Reject invalid month, year, months, and limit")
    void testParameterValidation() {
        assertThatThrownBy(() -> analyticsService.getDashboardSummary(1L, 13, 2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Month must be between 1 and 12");

        assertThatThrownBy(() -> analyticsService.getDashboardSummary(1L, 0, 2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Month must be between 1 and 12");

        assertThatThrownBy(() -> analyticsService.getDashboardSummary(1L, 5, 1999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Year must be between 2000 and 2100");

        assertThatThrownBy(() -> analyticsService.getMonthlyTrend(1L, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Months must be between 1 and 24");

        assertThatThrownBy(() -> analyticsService.getMonthlyTrend(1L, 25, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Months must be between 1 and 24");

        assertThatThrownBy(() -> analyticsService.getRecentExpenses(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 100");

        assertThatThrownBy(() -> analyticsService.getTopExpenses(1L, 10, 2026, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 100");
    }
}
