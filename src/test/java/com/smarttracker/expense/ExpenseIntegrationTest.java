package com.smarttracker.expense;

import com.smarttracker.auth.dto.AuthResponseDto;
import com.smarttracker.auth.dto.LoginRequestDto;
import com.smarttracker.auth.dto.RegisterRequestDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private String authUrl;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/expenses";
        authUrl = "http://localhost:" + port + "/api/auth";

        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login User 1
        RegisterRequestDto reg1 = new RegisterRequestDto("Alice", "alice@example.com", "Password123!");
        restTemplate.postForEntity(authUrl + "/register", reg1, Map.class);
        ResponseEntity<AuthResponseDto> login1 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("alice@example.com", "Password123!"), AuthResponseDto.class);
        user1Token = login1.getBody().accessToken();

        // Register and login User 2
        RegisterRequestDto reg2 = new RegisterRequestDto("Bob", "bob@example.com", "Password123!");
        restTemplate.postForEntity(authUrl + "/register", reg2, Map.class);
        ResponseEntity<AuthResponseDto> login2 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("bob@example.com", "Password123!"), AuthResponseDto.class);
        user2Token = login2.getBody().accessToken();
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
    @DisplayName("Complete CRUD lifecycle with authenticated user and PostgreSQL persistence")
    void testCompleteCrudLifecycle() {
        // 1. Create an expense as User 1
        ExpenseRequestDto createReq = new ExpenseRequestDto(
                "Coffee & Bagel",
                new BigDecimal("8.75"),
                "Food",
                "Morning breakfast",
                LocalDate.of(2026, 10, 11),
                "Apple Pay"
        );

        HttpEntity<ExpenseRequestDto> createEntity = new HttpEntity<>(createReq, createAuthHeaders(user1Token));
        ResponseEntity<ExpenseResponseDto> createResp = restTemplate.postForEntity(
                baseUrl, createEntity, ExpenseResponseDto.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ExpenseResponseDto created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Coffee & Bagel");
        assertThat(created.amount()).isEqualByComparingTo("8.75");
        assertThat(created.category()).isEqualTo("Food");
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();

        Long id = created.id();

        // 2. Fetch single expense as User 1
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(user1Token));
        ResponseEntity<ExpenseResponseDto> getResp = restTemplate.exchange(
                baseUrl + "/" + id, HttpMethod.GET, getEntity, ExpenseResponseDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody()).isNotNull();
        assertThat(getResp.getBody().id()).isEqualTo(id);
        assertThat(getResp.getBody().title()).isEqualTo("Coffee & Bagel");

        // 3. Update expense as User 1
        ExpenseRequestDto updateReq = new ExpenseRequestDto(
                "Coffee & 2 Bagels",
                new BigDecimal("12.50"),
                "Food",
                "Breakfast with friend",
                LocalDate.of(2026, 10, 11),
                "Debit Card"
        );

        HttpEntity<ExpenseRequestDto> updateEntity = new HttpEntity<>(updateReq, createAuthHeaders(user1Token));
        ResponseEntity<ExpenseResponseDto> updateResp = restTemplate.exchange(
                baseUrl + "/" + id, HttpMethod.PUT, updateEntity, ExpenseResponseDto.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).isNotNull();
        assertThat(updateResp.getBody().title()).isEqualTo("Coffee & 2 Bagels");
        assertThat(updateResp.getBody().amount()).isEqualByComparingTo("12.50");
        assertThat(updateResp.getBody().paymentMethod()).isEqualTo("Debit Card");

        // 4. Delete expense as User 1
        HttpEntity<Void> deleteEntity = new HttpEntity<>(createAuthHeaders(user1Token));
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl + "/" + id, HttpMethod.DELETE, deleteEntity, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 5. Verify deleted returns 404
        ResponseEntity<Map> verifyNotFound = restTemplate.exchange(
                baseUrl + "/" + id, HttpMethod.GET, getEntity, Map.class);
        assertThat(verifyNotFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Cross-user isolation: User 2 cannot access, update, or delete User 1's expense")
    void testCrossUserIsolation() {
        // User 1 creates an expense
        ExpenseRequestDto createReq = new ExpenseRequestDto(
                "Private Expense",
                new BigDecimal("99.99"),
                "Personal",
                "Secret purchase",
                LocalDate.of(2026, 10, 11),
                "Cash"
        );

        HttpEntity<ExpenseRequestDto> user1Create = new HttpEntity<>(createReq, createAuthHeaders(user1Token));
        ResponseEntity<ExpenseResponseDto> createResp = restTemplate.postForEntity(
                baseUrl, user1Create, ExpenseResponseDto.class);
        Long expenseId = createResp.getBody().id();

        // User 2 attempts to fetch User 1's expense -> 404 Not Found
        HttpEntity<Void> user2Get = new HttpEntity<>(createAuthHeaders(user2Token));
        ResponseEntity<Map> user2FetchResp = restTemplate.exchange(
                baseUrl + "/" + expenseId, HttpMethod.GET, user2Get, Map.class);
        assertThat(user2FetchResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 2 attempts to update User 1's expense -> 404 Not Found
        ExpenseRequestDto maliciousUpdate = new ExpenseRequestDto(
                "Tampered Title",
                new BigDecimal("1.00"),
                "Personal",
                null,
                LocalDate.of(2026, 10, 11),
                null
        );
        HttpEntity<ExpenseRequestDto> user2Update = new HttpEntity<>(maliciousUpdate, createAuthHeaders(user2Token));
        ResponseEntity<Map> user2UpdateResp = restTemplate.exchange(
                baseUrl + "/" + expenseId, HttpMethod.PUT, user2Update, Map.class);
        assertThat(user2UpdateResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 2 attempts to delete User 1's expense -> 404 Not Found
        ResponseEntity<Map> user2DeleteResp = restTemplate.exchange(
                baseUrl + "/" + expenseId, HttpMethod.DELETE, user2Get, Map.class);
        assertThat(user2DeleteResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 2 lists expenses -> returns empty page (totalElements = 0)
        ResponseEntity<Map> user2ListResp = restTemplate.exchange(
                baseUrl, HttpMethod.GET, user2Get, Map.class);
        assertThat(user2ListResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user2ListResp.getBody().get("totalElements")).isEqualTo(0);

        // Unauthenticated request without token -> 401 Unauthorized
        HttpEntity<Void> unauth = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<Map> unauthResp = restTemplate.exchange(
                baseUrl + "/" + expenseId, HttpMethod.GET, unauth, Map.class);
        assertThat(unauthResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filtering by category and date range is strictly user-scoped")
    void testFilteringAndPaginationUserScoped() {
        // User 1 creates 2 Transport expenses and 1 Food expense
        ExpenseRequestDto e1 = new ExpenseRequestDto("Train Ticket", new BigDecimal("35.00"), "Transport", null, LocalDate.of(2026, 10, 1), "Card");
        ExpenseRequestDto e2 = new ExpenseRequestDto("Bus Pass", new BigDecimal("15.00"), "Transport", null, LocalDate.of(2026, 10, 5), "Cash");
        ExpenseRequestDto e3 = new ExpenseRequestDto("Groceries", new BigDecimal("80.00"), "Food", null, LocalDate.of(2026, 10, 10), "Card");

        restTemplate.postForEntity(baseUrl, new HttpEntity<>(e1, createAuthHeaders(user1Token)), ExpenseResponseDto.class);
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(e2, createAuthHeaders(user1Token)), ExpenseResponseDto.class);
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(e3, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // User 2 also creates a Transport expense
        ExpenseRequestDto u2Exp = new ExpenseRequestDto("Flight Ticket", new BigDecimal("300.00"), "Transport", null, LocalDate.of(2026, 10, 2), "Card");
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(u2Exp, createAuthHeaders(user2Token)), ExpenseResponseDto.class);

        HttpEntity<Void> user1Entity = new HttpEntity<>(createAuthHeaders(user1Token));

        // User 1 filters Transport -> only gets their own 2 (Train Ticket, Bus Pass)
        ResponseEntity<Map> transportResp = restTemplate.exchange(
                baseUrl + "?category=Transport", HttpMethod.GET, user1Entity, Map.class);
        assertThat(transportResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transportResp.getBody().get("totalElements")).isEqualTo(2);

        // User 1 filters date range Oct 2 to Oct 8 -> only matches Bus Pass (Oct 5)
        ResponseEntity<Map> dateRangeResp = restTemplate.exchange(
                baseUrl + "?startDate=2026-10-02&endDate=2026-10-08", HttpMethod.GET, user1Entity, Map.class);
        assertThat(dateRangeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dateRangeResp.getBody().get("totalElements")).isEqualTo(1);
    }
}
