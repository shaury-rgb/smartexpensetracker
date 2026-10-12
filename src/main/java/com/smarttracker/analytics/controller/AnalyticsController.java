package com.smarttracker.analytics.controller;

import com.smarttracker.analytics.dto.BudgetStatusAnalyticsDto;
import com.smarttracker.analytics.dto.CategoryBreakdownDto;
import com.smarttracker.analytics.dto.DailyTrendDto;
import com.smarttracker.analytics.dto.DashboardSummaryDto;
import com.smarttracker.analytics.dto.MonthlyTrendDto;
import com.smarttracker.analytics.service.AnalyticsService;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.security.model.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year
    ) {
        DashboardSummaryDto summary = analyticsService.getDashboardSummary(principal.getId(), month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year
    ) {
        List<CategoryBreakdownDto> breakdown = analyticsService.getCategoryBreakdown(principal.getId(), month, year);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) Integer endMonth,
            @RequestParam(required = false) Integer endYear
    ) {
        List<MonthlyTrendDto> trend = analyticsService.getMonthlyTrend(principal.getId(), months, endMonth, endYear);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<List<DailyTrendDto>> getDailyTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year
    ) {
        List<DailyTrendDto> trend = analyticsService.getDailyTrend(principal.getId(), month, year);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/recent-expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getRecentExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<ExpenseResponseDto> recent = analyticsService.getRecentExpenses(principal.getId(), limit);
        return ResponseEntity.ok(recent);
    }

    @GetMapping("/top-expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getTopExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<ExpenseResponseDto> top = analyticsService.getTopExpenses(principal.getId(), month, year, limit);
        return ResponseEntity.ok(top);
    }

    @GetMapping("/budget-status")
    public ResponseEntity<BudgetStatusAnalyticsDto> getBudgetStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year
    ) {
        BudgetStatusAnalyticsDto status = analyticsService.getBudgetStatusAnalytics(principal.getId(), month, year);
        return ResponseEntity.ok(status);
    }
}
