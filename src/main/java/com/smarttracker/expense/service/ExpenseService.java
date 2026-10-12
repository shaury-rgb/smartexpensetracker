package com.smarttracker.expense.service;

import com.smarttracker.common.exception.ResourceNotFoundException;
import com.smarttracker.expense.dto.ExpenseRequestDto;
import com.smarttracker.expense.dto.ExpenseResponseDto;
import com.smarttracker.expense.entity.Expense;
import com.smarttracker.expense.repository.ExpenseRepository;
import com.smarttracker.expense.repository.ExpenseSpecification;
import com.smarttracker.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseResponseDto createExpense(User user, ExpenseRequestDto request) {
        Expense expense = new Expense(
                user,
                request.getTitle().trim(),
                request.getAmount(),
                request.getCategory().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getExpenseDate(),
                request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : null
        );

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        return ExpenseResponseDto.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpenses(Long userId, String category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        return expenseRepository.findAll(ExpenseSpecification.withFilters(userId, category, startDate, endDate), pageable)
                .map(ExpenseResponseDto::fromEntity);
    }

    public ExpenseResponseDto updateExpense(Long id, Long userId, ExpenseRequestDto request) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory().trim());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : null);

        Expense updated = expenseRepository.save(expense);
        return ExpenseResponseDto.fromEntity(updated);
    }

    public void deleteExpense(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        expenseRepository.delete(expense);
    }
}
