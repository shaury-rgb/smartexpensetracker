package com.smarttracker.budget.controller;

import com.smarttracker.budget.dto.BudgetRequestDto;
import com.smarttracker.budget.dto.BudgetResponseDto;
import com.smarttracker.budget.dto.BudgetUpdateRequestDto;
import com.smarttracker.budget.service.BudgetService;
import com.smarttracker.security.model.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDto> createBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BudgetRequestDto request
    ) {
        BudgetResponseDto created = budgetService.createBudget(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDto>> listBudgets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month,
            @RequestParam int year
    ) {
        List<BudgetResponseDto> budgets = budgetService.getBudgets(principal.getId(), month, year);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequestDto request
    ) {
        BudgetResponseDto updated = budgetService.updateBudget(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        budgetService.deleteBudget(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
