package com.housemate.backend.controller;

import com.housemate.backend.service.expense.ExpenseService;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpenseCreateRequestDTO expenseCreateRequest) {
        // Service handles all business logic and returns DTO
        ExpenseResponseDTO response = expenseService.createExpense(expenseCreateRequest);

        // Return as JSON response with code 201 (created)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getFilteredExpenses(@Valid @ModelAttribute ExpenseFilterRequestDTO filter) {
        
        List<ExpenseResponseDTO> expenses = expenseService.getFilteredExpenses(filter);
        return ResponseEntity.ok(expenses);
    }
}