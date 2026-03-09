package com.housemate.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.housemate.backend.service.expense.SettlementService;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    
    @PostMapping("/{debtId}")
    public ResponseEntity<SettlementResponseDTO> settleDebt(
            @PathVariable UUID debtId, 
            @Valid @RequestBody SettlementCreateRequestDTO request) {
        
        SettlementResponseDTO response = settlementService.settleDebt(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponseDTO>> getFilteredSettlements(
            @Valid @ModelAttribute SettlementFilterRequestDTO filter) {

        // Validate that at least one filter was provided to prevent fetching the entire DB
        if (filter.debtId() == null && filter.debtorId() == null 
            && filter.creditorId() == null && filter.involvedId() == null && filter.dateRange() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Pass the parameter object to the service
        List<SettlementResponseDTO> settlements = settlementService.getFilteredSettlements(filter);
        return ResponseEntity.ok(settlements);
    }
    
}
