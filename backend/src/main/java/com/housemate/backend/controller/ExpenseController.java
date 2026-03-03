package com.housemate.backend.controller;

import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing expenses and settlements.
 * 
 * Note: All @RequestBody parameters with request DTOs must include @Valid annotation
 * to trigger Jakarta Bean Validation on the incoming request payload.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    /**
     * Create a new expense with proper validation.
     * The @Valid annotation triggers validation of ExpenseCreateRequestDTO and its nested objects.
     * 
     * @param expenseCreateRequest the expense creation request DTO with validated fields
     * @return response entity with created expense details
     */
    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseCreateRequestDTO expenseCreateRequest) {
        // Implementation: Save expense to database
        // Validation errors will be automatically caught by Spring and returned as 400 Bad Request
        return ResponseEntity.ok().build();
    }

    /**
     * Settle a debt with proper validation.
     * The @Valid annotation triggers validation of SettlementCreateRequestDTO fields.
     * 
     * @param settlementRequest the settlement creation request DTO with validated fields
     * @return response entity with settlement confirmation
     */
    @PostMapping("/settle")
    public ResponseEntity<?> settleDebt(@Valid @RequestBody SettlementCreateRequestDTO settlementRequest) {
        // Implementation: Process settlement and update balances
        // Validation errors will be automatically caught by Spring and returned as 400 Bad Request
        return ResponseEntity.ok().build();
    }
}
