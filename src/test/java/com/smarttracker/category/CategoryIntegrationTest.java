package com.smarttracker.category;

import com.smarttracker.auth.dto.AuthResponseDto;
import com.smarttracker.auth.dto.LoginRequestDto;
import com.smarttracker.auth.dto.RegisterRequestDto;
import com.smarttracker.category.dto.CategoryRequestDto;
import com.smarttracker.category.dto.CategoryResponseDto;
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
class CategoryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
        baseUrl = "http://localhost:" + port + "/api/categories";
        authUrl = "http://localhost:" + port + "/api/auth";
        expenseUrl = "http://localhost:" + port + "/api/expenses";

        expenseRepository.deleteAll();
        categoryRepository.deleteCustomCategories();
        userRepository.deleteAll();

        // Register and login User 1
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Alice", "alice@cat.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login1 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("alice@cat.com", "Password123!"), AuthResponseDto.class);
        user1Token = login1.getBody().accessToken();

        // Register and login User 2
        restTemplate.postForEntity(authUrl + "/register",
                new RegisterRequestDto("Bob", "bob@cat.com", "Password123!"), Map.class);
        ResponseEntity<AuthResponseDto> login2 = restTemplate.postForEntity(
                authUrl + "/login", new LoginRequestDto("bob@cat.com", "Password123!"), AuthResponseDto.class);
        user2Token = login2.getBody().accessToken();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("List available categories includes system categories and user custom categories")
    void testListCategories() {
        // User 1 creates custom category
        CategoryRequestDto custom = new CategoryRequestDto("Crypto Investments");
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(custom, createAuthHeaders(user1Token)), CategoryResponseDto.class);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl, HttpMethod.GET, new HttpEntity<>(createAuthHeaders(user1Token)), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> categories = response.getBody();
        assertThat(categories).isNotNull();
        // At least 9 system categories + 1 custom
        assertThat(categories.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Duplicate category name for user is rejected")
    void testDuplicateCategoryRejected() {
        CategoryRequestDto custom = new CategoryRequestDto("Fitness");
        restTemplate.postForEntity(baseUrl, new HttpEntity<>(custom, createAuthHeaders(user1Token)), CategoryResponseDto.class);

        ResponseEntity<Map> dupResponse = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(new CategoryRequestDto("fitness"), createAuthHeaders(user1Token)), Map.class);

        assertThat(dupResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(dupResponse.getBody().get("message")).asString().contains("already exists");
    }

    @Test
    @DisplayName("Cannot delete custom category that is in use by expenses")
    void testCannotDeleteCategoryInUse() {
        // Create custom category
        CategoryRequestDto custom = new CategoryRequestDto("Subscriptions");
        ResponseEntity<CategoryResponseDto> created = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(custom, createAuthHeaders(user1Token)), CategoryResponseDto.class);
        Long categoryId = created.getBody().id();

        // Create an expense with this category
        ExpenseRequestDto expense = new ExpenseRequestDto(
                "Netflix", new BigDecimal("15.99"), "Subscriptions", null, LocalDate.of(2026, 10, 11), "Card");
        restTemplate.postForEntity(expenseUrl, new HttpEntity<>(expense, createAuthHeaders(user1Token)), ExpenseResponseDto.class);

        // Attempt to delete category -> 400 Bad Request
        ResponseEntity<Map> deleteResp = restTemplate.exchange(
                baseUrl + "/" + categoryId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(user1Token)), Map.class);

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(deleteResp.getBody().get("message")).asString().contains("Cannot delete category");
    }

    @Test
    @DisplayName("User 2 cannot rename or delete User 1's custom category")
    void testCrossUserCategoryIsolation() {
        CategoryRequestDto custom = new CategoryRequestDto("Private Hobby");
        ResponseEntity<CategoryResponseDto> created = restTemplate.postForEntity(
                baseUrl, new HttpEntity<>(custom, createAuthHeaders(user1Token)), CategoryResponseDto.class);
        Long categoryId = created.getBody().id();

        // User 2 attempts to rename User 1's category -> 404
        CategoryRequestDto rename = new CategoryRequestDto("Hijacked Hobby");
        ResponseEntity<Map> renameResp = restTemplate.exchange(
                baseUrl + "/" + categoryId, HttpMethod.PUT, new HttpEntity<>(rename, createAuthHeaders(user2Token)), Map.class);
        assertThat(renameResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // User 2 attempts to delete User 1's category -> 404
        ResponseEntity<Map> deleteResp = restTemplate.exchange(
                baseUrl + "/" + categoryId, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders(user2Token)), Map.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
