package com.housemate.backend.controller;

import com.housemate.backend.service.expense.DebtService;
import com.housemate.backend.service.expense.SettlementService;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;
    private final SettlementService settlementService; 

    /**
     * Get debts dynamically filtered by query parameters.
     */
    @GetMapping
    public ResponseEntity<List<DebtResponseDTO>> getFilteredDebts(
            @Valid @ModelAttribute DebtFilterRequestDTO filter) {

        // Validate that at least one filter was provided to prevent fetching the entire DB
        if (filter.householdId() == null && filter.debtorId() == null 
            && filter.creditorId() == null && filter.userId() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Pass the parameter object to the service
        List<DebtResponseDTO> debts = debtService.getFilteredDebts(filter);
        return ResponseEntity.ok(debts);
    }

    @PostMapping("/{debtId}/settlements")
    public ResponseEntity<SettlementResponseDTO> settleDebt(
            @PathVariable UUID debtId, 
            @Valid @RequestBody SettlementCreateRequestDTO request) {
        
        SettlementResponseDTO response = settlementService.settleDebt(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{debtId}")
    public ResponseEntity<Void> deleteDebt(@PathVariable UUID debtId) {
        debtService.deleteDebt(debtId);
        return ResponseEntity.noContent().build();
    }
}