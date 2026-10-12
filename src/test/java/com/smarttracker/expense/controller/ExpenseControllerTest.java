package com.smarttracker.expense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttracker.common.exception.GlobalExceptionHandler;
import com.smarttracker.common.exception.ResourceNotFoundException;
import com.smarttracker.expense.dto.ExpenseRequestDto;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.expense.service.ExpenseService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpenseController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserPrincipal testPrincipal;
    private ExpenseResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        User user = new User("Alice", "alice@example.com", "hash123");
        user.setId(10L);
        testPrincipal = new UserPrincipal(user);

        sampleResponse = new ExpenseResponseDto(
                1L,
                "Dinner",
                new BigDecimal("45.00"),
                "Food",
                "Italian dinner",
                LocalDate.of(2026, 10, 11),
                "Credit Card",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/expenses should return 201 Created when valid")
    void createExpense_shouldReturn201() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "Dinner",
                new BigDecimal("45.00"),
                "Food",
                "Italian dinner",
                LocalDate.of(2026, 10, 11),
                "Credit Card"
        );

        when(expenseService.createExpense(any(User.class), any(ExpenseRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/expenses")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Dinner"))
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    @DisplayName("POST /api/expenses should return 400 when title is blank")
    void createExpense_shouldReturn400_whenTitleBlank() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "",
                new BigDecimal("45.00"),
                "Food",
                null,
                LocalDate.of(2026, 10, 11),
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/expenses should return 400 when amount is zero or negative")
    void createExpense_shouldReturn400_whenAmountNonPositive() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "Lunch",
                new BigDecimal("0.00"),
                "Food",
                null,
                LocalDate.of(2026, 10, 11),
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/expenses should return 400 when expenseDate is null")
    void createExpense_shouldReturn400_whenExpenseDateNull() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "Lunch",
                new BigDecimal("20.00"),
                "Food",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.expenseDate").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/expenses should return 200 with paginated content")
    void listExpenses_shouldReturn200() throws Exception {
        Page<ExpenseResponseDto> page = new PageImpl<>(List.of(sampleResponse));
        when(expenseService.getExpenses(eq(10L), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/expenses")
                        .with(user(testPrincipal))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Dinner"));
    }

    @Test
    @DisplayName("GET /api/expenses should return 400 on invalid page parameters")
    void listExpenses_shouldReturn400_whenPageInvalid() throws Exception {
        mockMvc.perform(get("/api/expenses")
                        .with(user(testPrincipal))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero"));
    }

    @Test
    @DisplayName("GET /api/expenses/{id} should return 200 when found")
    void getExpenseById_shouldReturn200() throws Exception {
        when(expenseService.getExpenseById(1L, 10L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/expenses/1").with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Dinner"));
    }

    @Test
    @DisplayName("GET /api/expenses/{id} should return 404 when missing or wrong user")
    void getExpenseById_shouldReturn404() throws Exception {
        when(expenseService.getExpenseById(999L, 10L))
                .thenThrow(new ResourceNotFoundException("Expense not found with ID: 999"));

        mockMvc.perform(get("/api/expenses/999").with(user(testPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Expense not found with ID: 999"));
    }

    @Test
    @DisplayName("PUT /api/expenses/{id} should return 200 when updated")
    void updateExpense_shouldReturn200() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "Updated Dinner",
                new BigDecimal("55.00"),
                "Food",
                null,
                LocalDate.of(2026, 10, 11),
                null
        );

        when(expenseService.updateExpense(eq(1L), eq(10L), any(ExpenseRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/expenses/1")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PUT /api/expenses/{id} should return 404 when missing")
    void updateExpense_shouldReturn404() throws Exception {
        ExpenseRequestDto request = new ExpenseRequestDto(
                "Updated Dinner",
                new BigDecimal("55.00"),
                "Food",
                null,
                LocalDate.of(2026, 10, 11),
                null
        );

        when(expenseService.updateExpense(eq(999L), eq(10L), any(ExpenseRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Expense not found with ID: 999"));

        mockMvc.perform(put("/api/expenses/999")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} should return 204 No Content")
    void deleteExpense_shouldReturn204() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L, 10L);

        mockMvc.perform(delete("/api/expenses/1")
                        .with(user(testPrincipal))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} should return 404 when missing")
    void deleteExpense_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Expense not found with ID: 999"))
                .when(expenseService).deleteExpense(999L, 10L);

        mockMvc.perform(delete("/api/expenses/999")
                        .with(user(testPrincipal))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
