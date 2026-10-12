package com.smarttracker.budget;

import com.smarttracker.auth.dto.AuthResponseDto;
import com.smarttracker.auth.dto.LoginRequestDto;
import com.smarttracker.auth.dto.RegisterRequestDto;
import com.smarttracker.budget.dto.BudgetRequestDto;
import com.smarttracker.budget.dto.BudgetResponseDto;
import com.smarttracker.budget.dto.BudgetUpdateRequestDto;
import com.smarttracker.budget.repository.BudgetRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BudgetIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private String authUrl;
    private String expenseUrl;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/budgets";
        authUrl = "http://localhost:" + port + "/api/auth";
        expenseUrl = "http://localhost:" + port + "/api/expenses";

        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login User 1
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Alice", "alice@budget.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login1 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("alice@budget.com", "Password123!"), AuthResponseDto.class);
        user1Token = login1.getBody().accessToken();

        // Register and login User 2
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Bob", "bob@budget.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login2 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("bob@budget.com", "Password123!"), AuthResponseDto.class);
        user2Token = login2.getBody().accessToken();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Create overall and category budgets with live spending calculations")
    void testBudgetSpendingCalculation() {
        // 1. Create overall budget of 500 for Oct 2026
        BudgetRequestDto overallReq = new BudgetRequestDto(null, new BigDecimal("500.00"), 10, 2026);
        ResponseEntity<BudgetResponseDto> overallResp = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(overallReq, createAuthHeaders(user1Token)), BudgetResponseDto.class);

        assertThat(overallResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BudgetResponseDto createdOverall = overallResp.getBody();
        assertThat(createdOverall).isNotNull();
        assertThat(createdOverall.categoryId()).isNull();
        assertThat(createdOverall.amount()).isEqualByComparingTo("500.00");
        assertThat(createdOverall.actualSpending()).isEqualByComparingTo("0.00");
        assertThat(createdOverall.remainingAmount()).isEqualByComparingTo("500.00");
        assertThat(createdOverall.percentageUsed()).isEqualByComparingTo("0.00");

        // 2. Add an expense of 200 in Oct 2026
        ExpenseRequestDto expense1 = new ExpenseRequestDto(
                "Supermarket", new BigDecimal("200.00"), "Food", null, LocalDate.of(2026, 10, 5), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(expense1, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // 3. Query budgets for Oct 2026
        ResponseEntity<List> listResp = restTemplate.exchange(
                baseUrl + "?month=10&year=2026", HttpMethod.GET, new HttpEntity<>(createAuthHeaders(user1Token)), List.class);

        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> budgets = listResp.getBody();
        assertThat(budgets).hasSize(1);
        Map<?, ?> budgetMap = (Map<?, ?>) budgets.get(0);
        assertThat(new BigDecimal(budgetMap.get("actualSpending").toString())).isEqualByComparingTo("200.00");
        assertThat(new BigDecimal(budgetMap.get("remainingAmount").toString())).isEqualByComparingTo("300.00");
        assertThat(new BigDecimal(budgetMap.get("percentageUsed").toString())).isEqualByComparingTo("40.00");

        // 4. Overspend: add another expense of 400 (total spending 600 on 500 budget)
        ExpenseRequestDto expense2 = new ExpenseRequestDto(
                "Electronics", new BigDecimal("400.00"), "Shopping", null, LocalDate.of(2026, 10, 10), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(expense2, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        ResponseEntity<List> overspendResp = restTemplate.exchange(
                baseUrl + "?month=10&year=2026", HttpMethod.GET, new HttpEntity<>(createAuthHeaders(user1Token)), List.class);
        Map<?, ?> overspentMap = (Map<?, ?>) overspendResp.getBody().get(0);
        assertThat(new BigDecimal(overspentMap.get("actualSpending").toString())).isEqualByComparingTo("600.00");
        assertThat(new BigDecimal(overspentMap.get("remainingAmount").toString())).isEqualByComparingTo("-100.00");
        assertThat(new BigDecimal(overspentMap.get("percentageUsed").toString())).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("Prevent duplicate overall budget for same month and year")
    void testPreventDuplicateBudget() {
        BudgetRequestDto req = new BudgetRequestDto(null, new BigDecimal("300.00"), 11, 2026);
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(req, createAuthHeaders(user1Token)), BudgetResponseDto.class);

        ResponseEntity<Map> dupResp = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(req, createAuthHeaders(user1Token)), Map.class);

        assertThat(dupResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(dupResp.getBody().get("message")).asString().contains("already exists");
    }

    @Test
    @DisplayName("Cross-user budget isolation: User 2 cannot access, update, or delete User 1's budget")
    void testCrossUserBudgetIsolation() {
        BudgetRequestDto req = new BudgetRequestDto(null, new BigDecimal("450.00"), 12, 2026);
        ResponseEntity<BudgetResponseDto> created = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(req, createAuthHeaders(user1Token)), BudgetResponseDto.class);
        Long budgetId = created.getBody().id();

        // User 2 lists budgets for month 12/2026 -> returns empty list
        ResponseEntity<List> user2List = restTemplate.exchange(
                baseUrl + "?month=12&year=2026", HttpMethod.GET, new HttpEntity<>(createAuthHeaders(user2Token)), List.class);
        assertThat(user2List.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user2List.getBody()).isEmpty();

        // User 2 attempts to update User 1's budget -> 404
        BudgetUpdateRequestDto update = new BudgetUpdateRequestDto(new BigDecimal("1000.00"));
        ResponseEntity<Map> updateResp = restTemplate.exchange(
                baseUrl + "/" + budgetId, HttpMethod.PUT, new HttpEntity<>(update, createAuthHeaders(user2Token)), Map.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 2 attempts to delete User 1's budget -> 404
        ResponseEntity<Map> deleteResp = restTemplate.exchange(
                baseUrl + "/" + budgetId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(user2Token)), Map.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 1 updates their budget -> 200 OK
        ResponseEntity<BudgetResponseDto> u1Update = restTemplate.exchange(
                baseUrl + "/" + budgetId, HttpMethod.PUT, new HttpEntity<>(update, createAuthHeaders(user1Token)), BudgetResponseDto.class);
        assertThat(u1Update.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(u1Update.getBody().amount()).isEqualByComparingTo("1000.00");

        // User 1 deletes their budget -> 204 No Content
        ResponseEntity<Void> u1Delete = restTemplate.exchange(
                baseUrl + "/" + budgetId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(user1Token)), Void.class);
        assertThat(u1Delete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
