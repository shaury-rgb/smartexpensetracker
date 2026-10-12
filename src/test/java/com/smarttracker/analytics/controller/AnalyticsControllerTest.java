package com.smarttracker.analytics.controller;

import com.smarttracker.analytics.dto.BudgetStatusAnalyticsDto;
import com.smarttracker.analytics.dto.CategoryBreakdownDto;
import com.smarttracker.analytics.dto.DailyTrendDto;
import com.smarttracker.analytics.dto.DashboardSummaryDto;
import com.smarttracker.analytics.dto.MonthlyTrendDto;
import com.smarttracker.analytics.dto.OverallBudgetStatusDto;
import com.smarttracker.analytics.service.AnalyticsService;
import com.smarttracker.common.exception.GlobalExceptionHandler;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.security.config.SecurityConfig;
import com.smarttracker.security.jwt.JwtAuthenticationEntryPoint;
import com.smarttracker.security.jwt.JwtTokenProvider;
import com.smarttracker.security.model.UserPrincipal;
import com.smarttracker.security.service.CustomUserDetailsService;
import com.smarttracker.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        User user = new User("Alice", "alice@example.com", "hash123");
        user.setId(10L);
        testPrincipal = new UserPrincipal(user);
    }

    @Test
    @DisplayName("GET /api/analytics/summary should return 200 with summary data")
    void testGetSummary() throws Exception {
        DashboardSummaryDto summary = new DashboardSummaryDto(
                10,
                2026,
                new BigDecimal("250.00"),
                5,
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                new BigDecimal("20.00"),
                new BigDecimal("200.00"),
                new BigDecimal("50.00"),
                new BigDecimal("25.00"),
                new BigDecimal("500.00"),
                new BigDecimal("250.00")
        );

        when(analyticsService.getDashboardSummary(10L, 10, 2026)).thenReturn(summary);

        mockMvc.perform(get("/api/analytics/summary")
                        .with(user(testPrincipal))
                        .param("month", "10")
                        .param("year", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalSpending").value(250.00))
                .andExpect(jsonPath("$.expenseCount").value(5))
                .andExpect(jsonPath("$.averageExpenseAmount").value(50.00))
                .andExpect(jsonPath("$.percentageChange").value(25.00));
    }

    @Test
    @DisplayName("GET /api/analytics/category-breakdown should return 200 with category list")
    void testGetCategoryBreakdown() throws Exception {
        List<CategoryBreakdownDto> breakdown = List.of(
                new CategoryBreakdownDto("Food", new BigDecimal("150.00"), 3, new BigDecimal("60.00")),
                new CategoryBreakdownDto("Transport", new BigDecimal("100.00"), 2, new BigDecimal("40.00"))
        );

        when(analyticsService.getCategoryBreakdown(10L, 10, 2026)).thenReturn(breakdown);

        mockMvc.perform(get("/api/analytics/category-breakdown")
                        .with(user(testPrincipal))
                        .param("month", "10")
                        .param("year", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].percentage").value(60.00))
                .andExpect(jsonPath("$[1].categoryName").value("Transport"))
                .andExpect(jsonPath("$[1].percentage").value(40.00));
    }

    @Test
    @DisplayName("GET /api/analytics/monthly-trend should return 200")
    void testGetMonthlyTrend() throws Exception {
        List<MonthlyTrendDto> trend = List.of(
                new MonthlyTrendDto(2026, 9, "SEP 2026", new BigDecimal("200.00"), 4),
                new MonthlyTrendDto(2026, 10, "OCT 2026", new BigDecimal("250.00"), 5)
        );

        when(analyticsService.getMonthlyTrend(eq(10L), eq(6), eq(null), eq(null))).thenReturn(trend);

        mockMvc.perform(get("/api/analytics/monthly-trend")
                        .with(user(testPrincipal))
                        .param("months", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monthLabel").value("SEP 2026"))
                .andExpect(jsonPath("$[1].monthLabel").value("OCT 2026"));
    }

    @Test
    @DisplayName("GET /api/analytics/daily-trend should return 200")
    void testGetDailyTrend() throws Exception {
        List<DailyTrendDto> dailyTrend = List.of(
                new DailyTrendDto(LocalDate.of(2026, 10, 1), 1, new BigDecimal("25.00"), 1),
                new DailyTrendDto(LocalDate.of(2026, 10, 2), 2, BigDecimal.ZERO, 0)
        );

        when(analyticsService.getDailyTrend(10L, 10, 2026)).thenReturn(dailyTrend);

        mockMvc.perform(get("/api/analytics/daily-trend")
                        .with(user(testPrincipal))
                        .param("month", "10")
                        .param("year", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value(1))
                .andExpect(jsonPath("$[0].totalSpending").value(25.00))
                .andExpect(jsonPath("$[1].day").value(2))
                .andExpect(jsonPath("$[1].totalSpending").value(0));
    }

    @Test
    @DisplayName("GET /api/analytics/recent-expenses should return 200")
    void testGetRecentExpenses() throws Exception {
        ExpenseResponseDto expense = new ExpenseResponseDto(
                1L, "Dinner", new BigDecimal("45.00"), "Food", null,
                LocalDate.of(2026, 10, 11), "Card", OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(analyticsService.getRecentExpenses(10L, 5)).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/analytics/recent-expenses")
                        .with(user(testPrincipal))
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Dinner"));
    }

    @Test
    @DisplayName("GET /api/analytics/top-expenses should return 200")
    void testGetTopExpenses() throws Exception {
        ExpenseResponseDto expense = new ExpenseResponseDto(
                2L, "Laptop", new BigDecimal("1200.00"), "Work", null,
                LocalDate.of(2026, 10, 5), "Card", OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(analyticsService.getTopExpenses(10L, 10, 2026, 5)).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/analytics/top-expenses")
                        .with(user(testPrincipal))
                        .param("month", "10")
                        .param("year", "2026")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].amount").value(1200.00));
    }

    @Test
    @DisplayName("GET /api/analytics/budget-status should return 200")
    void testGetBudgetStatus() throws Exception {
        OverallBudgetStatusDto overall = new OverallBudgetStatusDto(
                true, new BigDecimal("1000.00"), new BigDecimal("350.00"),
                new BigDecimal("650.00"), new BigDecimal("35.00"), false
        );
        BudgetStatusAnalyticsDto budgetStatus = new BudgetStatusAnalyticsDto(
                10, 2026, overall, Collections.emptyList()
        );

        when(analyticsService.getBudgetStatusAnalytics(10L, 10, 2026)).thenReturn(budgetStatus);

        mockMvc.perform(get("/api/analytics/budget-status")
                        .with(user(testPrincipal))
                        .param("month", "10")
                        .param("year", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.overallBudget.configured").value(true))
                .andExpect(jsonPath("$.overallBudget.budgetAmount").value(1000.00))
                .andExpect(jsonPath("$.overallBudget.percentageUsed").value(35.00))
                .andExpect(jsonPath("$.overallBudget.exceeded").value(false));
    }

    @Test
    @DisplayName("Unauthenticated request should return 401 Unauthorized")
    void testUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/summary")
                        .param("month", "10")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }
}
