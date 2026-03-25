package com.housemate.backend.controller;

import com.housemate.backend.service.expense.ExpenseService;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
        @Valid @RequestBody ExpenseCreateRequestDTO expenseCreateRequest,
        @AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);    
        // Service handles all business logic and returns DTO
        ExpenseResponseDTO response = expenseService.createExpense(
            Objects.requireNonNull(userId),
            Objects.requireNonNull(expenseCreateRequest)
        );

        // Return as JSON response with code 201 (created)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getFilteredExpenses(
        @Valid @ModelAttribute TransactionFilterRequestDTO filter,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);
        
        List<ExpenseResponseDTO> expenses = expenseService.getFilteredExpenses(
            Objects.requireNonNull(userId),
            Objects.requireNonNull(filter)
        );
        return ResponseEntity.ok(expenses);
    }
}