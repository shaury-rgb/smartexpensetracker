package com.smarttracker.analytics;

import com.smarttracker.analytics.dto.BudgetStatusAnalyticsDto;
import com.smarttracker.analytics.dto.CategoryBreakdownDto;
import com.smarttracker.analytics.dto.DailyTrendDto;
import com.smarttracker.analytics.dto.DashboardSummaryDto;
import com.smarttracker.analytics.dto.MonthlyTrendDto;
import com.smarttracker.auth.dto.AuthResponseDto;
import com.smarttracker.auth.dto.LoginRequestDto;
import com.smarttracker.auth.dto.RegisterRequestDto;
import com.smarttracker.budget.dto.BudgetRequestDto;
import com.smarttracker.budget.repository.BudgetRepository;
import com.smarttracker.category.entity.Category;
import com.smarttracker.category.repository.CategoryRepository;
import com.smarttracker.expense.dto.ExpenseRequestDto;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.expense.repository.ExpenseRepository;
import com.smarttracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalyticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private String authUrl;
    private String expenseUrl;
    private String budgetUrl;

    private String user1Token;
    private String user2Token;
    private Long foodCategoryId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/analytics";
        authUrl = "http://localhost:" + port + "/api/auth";
        expenseUrl = "http://localhost:" + port + "/api/expenses";
        budgetUrl = "http://localhost:" + port + "/api/budgets";

        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        categoryRepository.deleteCustomCategories();
        userRepository.deleteAll();

        // Register and login User 1
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Alice", "alice@analytics.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login1 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("alice@analytics.com", "Password123!"), AuthResponseDto.class);
        user1Token = login1.getBody().accessToken();

        // Register and login User 2
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Bob", "bob@analytics.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login2 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("bob@analytics.com", "Password123!"), AuthResponseDto.class);
        user2Token = login2.getBody().accessToken();

        Category foodCat = categoryRepository.findAll().stream()
                .filter(c -> "Food".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(null);
        if (foodCat != null) {
            foodCategoryId = foodCat.getId();
        }
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    @Test
    @DisplayName("Complete analytics lifecycle: summary, category breakdown, monthly trend, daily trend, recent/top expenses, budget status")
    void testCompleteAnalyticsLifecycle() {
        // --- 1. Populate User 1 data ---
        // Expenses in Oct 2026:
        // 1. Food 60.00 on 2026-10-02
        ExpenseRequestDto e1 = new ExpenseRequestDto("Groceries", new BigDecimal("60.00"), "Food", null, LocalDate.of(2026, 10, 2), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(e1, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // 2. Transport 40.00 on 2026-10-10
        ExpenseRequestDto e2 = new ExpenseRequestDto("Gas", new BigDecimal("40.00"), "Transportation", null, LocalDate.of(2026, 10, 10), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(e2, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // Expense in Sep 2026: Food 50.00 on 2026-09-15
        ExpenseRequestDto ePrev = new ExpenseRequestDto("Dinner", new BigDecimal("50.00"), "Food", null, LocalDate.of(2026, 9, 15), "Cash");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(ePrev, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // Budgets for User 1 in Oct 2026:
        // Overall: 150.00
        BudgetRequestDto bOverall = new BudgetRequestDto(null, new BigDecimal("150.00"), 10, 2026);
        restTemplate.postForEntity(budgetUrl, new HttpEntity<>(bOverall, createAuthHeaders(user1Token)), Map.class);

        // Food category budget: 50.00
        if (foodCategoryId != null) {
            BudgetRequestDto bFood = new BudgetRequestDto(foodCategoryId, new BigDecimal("50.00"), 10, 2026);
            restTemplate.postForEntity(budgetUrl, new HttpEntity<>(bFood, createAuthHeaders(user1Token)), Map.class);
        }

        // --- 2. Populate User 2 data (should never leak to User 1) ---
        ExpenseRequestDto u2Exp = new ExpenseRequestDto("Flight", new BigDecimal("500.00"), "Transportation", null, LocalDate.of(2026, 10, 5), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(u2Exp, createAuthHeaders(user2Token)), ExpenseResponseDto.class);

        // ==========================================
        // 3. Verify GET /api/analytics/summary
        // ==========================================
        ResponseEntity<DashboardSummaryDto> summaryResp = restTemplate.exchange(
                baseUrl + "/summary?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                DashboardSummaryDto.class
        );

        assertThat(summaryResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        DashboardSummaryDto summary = summaryResp.getBody();
        assertThat(summary).isNotNull();
        assertThat(summary.month()).isEqualTo(10);
        assertThat(summary.year()).isEqualTo(2026);
        assertThat(summary.totalSpending()).isEqualByComparingTo("100.00");
        assertThat(summary.expenseCount()).isEqualTo(2L);
        assertThat(summary.averageExpenseAmount()).isEqualByComparingTo("50.00");
        assertThat(summary.highestExpenseAmount()).isEqualByComparingTo("60.00");
        assertThat(summary.lowestExpenseAmount()).isEqualByComparingTo("40.00");
        assertThat(summary.previousMonthTotalSpending()).isEqualByComparingTo("50.00");
        assertThat(summary.absoluteChange()).isEqualByComparingTo("50.00");
        assertThat(summary.percentageChange()).isEqualByComparingTo("100.00");
        assertThat(summary.budgetAmount()).isEqualByComparingTo("150.00");
        assertThat(summary.remainingBudget()).isEqualByComparingTo("50.00");

        // ==========================================
        // 4. Verify GET /api/analytics/category-breakdown
        // ==========================================
        ResponseEntity<List<CategoryBreakdownDto>> breakdownResp = restTemplate.exchange(
                baseUrl + "/category-breakdown?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(breakdownResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CategoryBreakdownDto> breakdown = breakdownResp.getBody();
        assertThat(breakdown).hasSize(2);
        // Food 60.00 (60%), Transport 40.00 (40%)
        assertThat(breakdown.get(0).categoryName()).isEqualTo("Food");
        assertThat(breakdown.get(0).totalSpending()).isEqualByComparingTo("60.00");
        assertThat(breakdown.get(0).percentage()).isEqualByComparingTo("60.00");

        assertThat(breakdown.get(1).categoryName()).isEqualTo("Transportation");
        assertThat(breakdown.get(1).totalSpending()).isEqualByComparingTo("40.00");
        assertThat(breakdown.get(1).percentage()).isEqualByComparingTo("40.00");

        // ==========================================
        // 5. Verify GET /api/analytics/monthly-trend
        // ==========================================
        ResponseEntity<List<MonthlyTrendDto>> trendResp = restTemplate.exchange(
                baseUrl + "/monthly-trend?months=3&endMonth=10&endYear=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(trendResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MonthlyTrendDto> monthlyTrend = trendResp.getBody();
        assertThat(monthlyTrend).hasSize(3);
        // Aug 2026: 0.00
        assertThat(monthlyTrend.get(0).month()).isEqualTo(8);
        assertThat(monthlyTrend.get(0).totalSpending()).isEqualByComparingTo("0.00");
        // Sep 2026: 50.00
        assertThat(monthlyTrend.get(1).month()).isEqualTo(9);
        assertThat(monthlyTrend.get(1).totalSpending()).isEqualByComparingTo("50.00");
        // Oct 2026: 100.00
        assertThat(monthlyTrend.get(2).month()).isEqualTo(10);
        assertThat(monthlyTrend.get(2).totalSpending()).isEqualByComparingTo("100.00");

        // ==========================================
        // 6. Verify GET /api/analytics/daily-trend
        // ==========================================
        ResponseEntity<List<DailyTrendDto>> dailyResp = restTemplate.exchange(
                baseUrl + "/daily-trend?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(dailyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<DailyTrendDto> dailyTrend = dailyResp.getBody();
        assertThat(dailyTrend).hasSize(31);
        // Day 2: 60.00
        assertThat(dailyTrend.get(1).day()).isEqualTo(2);
        assertThat(dailyTrend.get(1).totalSpending()).isEqualByComparingTo("60.00");
        // Day 10: 40.00
        assertThat(dailyTrend.get(9).day()).isEqualTo(10);
        assertThat(dailyTrend.get(9).totalSpending()).isEqualByComparingTo("40.00");
        // Day 1: 0.00
        assertThat(dailyTrend.get(0).totalSpending()).isEqualByComparingTo("0.00");

        // ==========================================
        // 7. Verify GET /api/analytics/top-expenses
        // ==========================================
        ResponseEntity<List<ExpenseResponseDto>> topResp = restTemplate.exchange(
                baseUrl + "/top-expenses?month=10&year=2026&limit=2",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(topResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ExpenseResponseDto> topExpenses = topResp.getBody();
        assertThat(topExpenses).hasSize(2);
        assertThat(topExpenses.get(0).amount()).isEqualByComparingTo("60.00");
        assertThat(topExpenses.get(1).amount()).isEqualByComparingTo("40.00");

        // ==========================================
        // 8. Verify GET /api/analytics/recent-expenses
        // ==========================================
        ResponseEntity<List<ExpenseResponseDto>> recentResp = restTemplate.exchange(
                baseUrl + "/recent-expenses?limit=2",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(recentResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ExpenseResponseDto> recentExpenses = recentResp.getBody();
        assertThat(recentExpenses).hasSize(2);

        // ==========================================
        // 9. Verify GET /api/analytics/budget-status
        // ==========================================
        ResponseEntity<BudgetStatusAnalyticsDto> budgetStatusResp = restTemplate.exchange(
                baseUrl + "/budget-status?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user1Token)),
                BudgetStatusAnalyticsDto.class
        );

        assertThat(budgetStatusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        BudgetStatusAnalyticsDto budgetStatus = budgetStatusResp.getBody();
        assertThat(budgetStatus).isNotNull();
        // Overall: 150.00 budget, 100.00 spent, 50.00 remaining, 66.67% used, exceeded = false
        assertThat(budgetStatus.overallBudget().configured()).isTrue();
        assertThat(budgetStatus.overallBudget().budgetAmount()).isEqualByComparingTo("150.00");
        assertThat(budgetStatus.overallBudget().actualSpending()).isEqualByComparingTo("100.00");
        assertThat(budgetStatus.overallBudget().remainingAmount()).isEqualByComparingTo("50.00");
        assertThat(budgetStatus.overallBudget().percentageUsed()).isEqualByComparingTo("66.67");
        assertThat(budgetStatus.overallBudget().exceeded()).isFalse();

        // Food: 50.00 budget, 60.00 spent -> exceeded = true!
        if (foodCategoryId != null) {
            assertThat(budgetStatus.categoryBudgets()).hasSize(1);
            assertThat(budgetStatus.categoryBudgets().get(0).categoryName()).isEqualTo("Food");
            assertThat(budgetStatus.categoryBudgets().get(0).budgetAmount()).isEqualByComparingTo("50.00");
            assertThat(budgetStatus.categoryBudgets().get(0).actualSpending()).isEqualByComparingTo("60.00");
            assertThat(budgetStatus.categoryBudgets().get(0).remainingAmount()).isEqualByComparingTo("-10.00");
            assertThat(budgetStatus.categoryBudgets().get(0).percentageUsed()).isEqualByComparingTo("120.00");
            assertThat(budgetStatus.categoryBudgets().get(0).exceeded()).isTrue();
        }
    }

    @Test
    @DisplayName("Cross-user isolation: User 2 sees only their own analytics data")
    void testCrossUserIsolation() {
        // User 1 creates an expense
        ExpenseRequestDto u1Exp = new ExpenseRequestDto("Secret Item", new BigDecimal("1000.00"), "Personal", null, LocalDate.of(2026, 10, 1), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(u1Exp, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // User 2 queries summary for Oct 2026 -> should have 0.00 spending
        ResponseEntity<DashboardSummaryDto> u2Summary = restTemplate.exchange(
                baseUrl + "/summary?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user2Token)),
                DashboardSummaryDto.class
        );
        assertThat(u2Summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(u2Summary.getBody().totalSpending()).isEqualByComparingTo("0.00");
        assertThat(u2Summary.getBody().expenseCount()).isEqualTo(0L);

        // User 2 queries top expenses -> should be empty
        ResponseEntity<List> u2Top = restTemplate.exchange(
                baseUrl + "/top-expenses?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user2Token)),
                List.class
        );
        assertThat(u2Top.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(u2Top.getBody()).isEmpty();

        // User 2 queries budget status -> overall not configured, category budgets empty
        ResponseEntity<BudgetStatusAnalyticsDto> u2Budget = restTemplate.exchange(
                baseUrl + "/budget-status?month=10&year=2026",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user2Token)),
                BudgetStatusAnalyticsDto.class
        );
        assertThat(u2Budget.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(u2Budget.getBody().overallBudget().configured()).isFalse();
        assertThat(u2Budget.getBody().categoryBudgets()).isEmpty();
    }

    @Test
    @DisplayName("Invalid parameters return HTTP 400 Bad Request")
    void testInvalidParameters() {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(user1Token));

        // Invalid month
        ResponseEntity<Map> respMonth = restTemplate.exchange(
                baseUrl + "/summary?month=13&year=2026", HttpMethod.GET, entity, Map.class);
        assertThat(respMonth.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Invalid limit
        ResponseEntity<Map> respLimit = restTemplate.exchange(
                baseUrl + "/recent-expenses?limit=0", HttpMethod.GET, entity, Map.class);
        assertThat(respLimit.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Invalid months
        ResponseEntity<Map> respMonths = restTemplate.exchange(
                baseUrl + "/monthly-trend?months=25", HttpMethod.GET, entity, Map.class);
        assertThat(respMonths.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Unauthorized requests return HTTP 401")
    void testUnauthorizedRequest() {
        ResponseEntity<Map> unauth = restTemplate.getForEntity(baseUrl + "/summary?month=10&year=2026", Map.class);
        assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
