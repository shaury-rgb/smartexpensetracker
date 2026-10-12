package com.smarttracker.auth;

import com.smarttracker.auth.dto.AuthResponseDto;
import com.smarttracker.auth.dto.LoginRequestDto;
import com.smarttracker.auth.dto.RegisterRequestDto;
import com.smarttracker.user.dto.UserResponseDto;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("User registration succeeds and does not expose password hash")
    void testRegistrationSuccess() {
        RegisterRequestDto request = new RegisterRequestDto("John Doe", "john@example.com", "SecurePass123!");
        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/register", request, UserResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("John Doe");
        assertThat(body.email()).isEqualTo("john@example.com");
        assertThat(body.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("Duplicate email registration is rejected with 400 Bad Request")
    void testDuplicateEmailRegistrationFails() {
        RegisterRequestDto request = new RegisterRequestDto("John Doe", "john@example.com", "SecurePass123!");
        restTemplate.postForEntity(baseUrl + "/register", request, UserResponseDto.class);

        RegisterRequestDto duplicate = new RegisterRequestDto("Another John", "JOHN@example.com", "AnotherPass123!");
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/register", duplicate, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Email is already registered");
    }

    @Test
    @DisplayName("Registration validation rejects invalid email and short password")
    @SuppressWarnings("unchecked")
    void testRegistrationValidation() {
        RegisterRequestDto invalid = new RegisterRequestDto("", "not-an-email", "short");
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/register", invalid, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> fieldErrors = (Map<String, Object>) response.getBody().get("fieldErrors");
        assertThat(fieldErrors).containsKey("name");
        assertThat(fieldErrors).containsKey("email");
        assertThat(fieldErrors).containsKey("password");
    }

    @Test
    @DisplayName("Login with valid credentials returns JWT access token")
    void testLoginSuccess() {
        RegisterRequestDto register = new RegisterRequestDto("Jane", "jane@example.com", "StrongPassword123!");
        restTemplate.postForEntity(baseUrl + "/register", register, UserResponseDto.class);

        LoginRequestDto login = new LoginRequestDto("jane@example.com", "StrongPassword123!");
        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/login", login, AuthResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isNotBlank();
        assertThat(body.tokenType()).isEqualTo("Bearer");
        assertThat(body.user().email()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Login with incorrect password returns 401 Unauthorized")
    void testLoginIncorrectPassword() {
        RegisterRequestDto register = new RegisterRequestDto("Jane", "jane@example.com", "StrongPassword123!");
        restTemplate.postForEntity(baseUrl + "/register", register, UserResponseDto.class);

        LoginRequestDto login = new LoginRequestDto("jane@example.com", "WrongPassword!");
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/login", login, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Invalid email or password");
    }

    @Test
    @DisplayName("GET /api/auth/me returns profile when token is valid and 401 when missing or tampered")
    void testCurrentUserProfileEndpoint() {
        RegisterRequestDto register = new RegisterRequestDto("Alice", "alice@example.com", "Password123!");
        restTemplate.postForEntity(baseUrl + "/register", register, UserResponseDto.class);

        LoginRequestDto login = new LoginRequestDto("alice@example.com", "Password123!");
        ResponseEntity<AuthResponseDto> loginResp = restTemplate.postForEntity(
                baseUrl + "/login", login, AuthResponseDto.class);
        String token = loginResp.getBody().accessToken();

        // Valid token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<UserResponseDto> meResp = restTemplate.exchange(
                baseUrl + "/me", HttpMethod.GET, new HttpEntity<>(headers), UserResponseDto.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResp.getBody().email()).isEqualTo("alice@example.com");

        // Missing token -> 401
        ResponseEntity<Map> unauthResp = restTemplate.exchange(
                baseUrl + "/me", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Map.class);
        assertThat(unauthResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Tampered token -> 401
        HttpHeaders badHeaders = new HttpHeaders();
        badHeaders.setBearerAuth("invalid.jwt.token.string");
        ResponseEntity<Map> badTokenResp = restTemplate.exchange(
                baseUrl + "/me", HttpMethod.GET, new HttpEntity<>(badHeaders), Map.class);
        assertThat(badTokenResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
