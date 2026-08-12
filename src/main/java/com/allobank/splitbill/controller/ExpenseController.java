package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@PathVariable UUID groupId, @Valid @RequestBody AddExpenseRequest request) {
        Expense expense = expenseService.addExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.from(expense));
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(@PathVariable UUID groupId) {
        return expenseService.getExpensesForGroup(groupId).stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }
}
