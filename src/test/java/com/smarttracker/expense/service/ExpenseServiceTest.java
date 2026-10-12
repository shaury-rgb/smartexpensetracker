package com.smarttracker.expense.service;

import com.smarttracker.common.exception.ResourceNotFoundException;
import com.smarttracker.expense.dto.ExpenseRequestDto;
import com.smarttracker.expense.dto.ExpenseResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Expense testExpense;
    private ExpenseRequestDto testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("Alice", "alice@example.com", "hash123");
        testUser.setId(10L);

        testExpense = new Expense(
                testUser,
                "Groceries",
                new BigDecimal("120.50"),
                "Food",
                "Weekly supermarket run",
                LocalDate.of(2026, 10, 10),
                "Credit Card"
        );
        testExpense.setId(1L);
        testExpense.setCreatedAt(OffsetDateTime.now());
        testExpense.setUpdatedAt(OffsetDateTime.now());

        testRequest = new ExpenseRequestDto(
                "Groceries",
                new BigDecimal("120.50"),
                "Food",
                "Weekly supermarket run",
                LocalDate.of(2026, 10, 10),
                "Credit Card"
        );
    }

    @Test
    @DisplayName("createExpense should persist and return created expense for user")
    void createExpense_shouldSucceed() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(1L);
            e.setCreatedAt(OffsetDateTime.now());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });

        ExpenseResponseDto response = expenseService.createExpense(testUser, testRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Groceries");
        assertThat(response.amount()).isEqualByComparingTo("120.50");
        assertThat(response.category()).isEqualTo("Food");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("getExpenseById should return expense when found for user")
    void getExpenseById_shouldReturnExpense() {
        when(expenseRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(testExpense));

        ExpenseResponseDto response = expenseService.getExpenseById(1L, 10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("getExpenseById should throw ResourceNotFoundException when missing or wrong user")
    void getExpenseById_shouldThrowWhenNotFound() {
        when(expenseRepository.findByIdAndUserId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(999L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with ID: 999");
    }

    @Test
    @DisplayName("getExpenses should reject invalid date range where startDate is after endDate")
    void getExpenses_shouldThrowWhenStartDateAfterEndDate() {
        LocalDate start = LocalDate.of(2026, 10, 15);
        LocalDate end = LocalDate.of(2026, 10, 10);
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> expenseService.getExpenses(10L, null, start, end, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startDate cannot be after endDate");
    }

    @Test
    @DisplayName("getExpenses should return paginated list scoped to user")
    void getExpenses_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> page = new PageImpl<>(List.of(testExpense), pageable, 1);
        when(expenseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ExpenseResponseDto> result = expenseService.getExpenses(10L, "Food", null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("updateExpense should update existing fields and return updated expense")
    void updateExpense_shouldSucceed() {
        when(expenseRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseRequestDto updateReq = new ExpenseRequestDto(
                "Updated Groceries",
                new BigDecimal("150.00"),
                "Groceries",
                "Updated desc",
                LocalDate.of(2026, 10, 11),
                "Debit Card"
        );

        ExpenseResponseDto response = expenseService.updateExpense(1L, 10L, updateReq);

        assertThat(response.title()).isEqualTo("Updated Groceries");
        assertThat(response.amount()).isEqualByComparingTo("150.00");
        assertThat(response.category()).isEqualTo("Groceries");
        assertThat(response.paymentMethod()).isEqualTo("Debit Card");
    }

    @Test
    @DisplayName("updateExpense should throw ResourceNotFoundException when not found or wrong user")
    void updateExpense_shouldThrowWhenNotFound() {
        when(expenseRepository.findByIdAndUserId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(999L, 10L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with ID: 999");
    }

    @Test
    @DisplayName("deleteExpense should delete expense when found for user")
    void deleteExpense_shouldSucceed() {
        when(expenseRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(1L, 10L);

        verify(expenseRepository).delete(testExpense);
    }

    @Test
    @DisplayName("deleteExpense should throw ResourceNotFoundException when not found or wrong user")
    void deleteExpense_shouldThrowWhenNotFound() {
        when(expenseRepository.findByIdAndUserId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(999L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with ID: 999");
    }
}
