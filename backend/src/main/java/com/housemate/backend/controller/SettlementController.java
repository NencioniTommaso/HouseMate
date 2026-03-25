package com.housemate.backend.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.housemate.backend.service.expense.SettlementService;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Validated
public class SettlementController {

    private final SettlementService settlementService;
    
    @PostMapping("/{debtId}")
    public ResponseEntity<SettlementResponseDTO> settleDebt(
            @PathVariable UUID debtId, 
            @Valid @RequestBody SettlementCreateRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        SettlementResponseDTO response = settlementService.settleDebt(
            Objects.requireNonNull(userId),
            Objects.requireNonNull(request)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponseDTO>> getFilteredSettlements(
            @Valid @ModelAttribute TransactionFilterRequestDTO filter,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);            //Exception catched by global handler

        // Pass the parameter object to the service
        List<SettlementResponseDTO> settlements = settlementService.getFilteredSettlements(
            Objects.requireNonNull(userId),
            Objects.requireNonNull(filter)
        );
        return ResponseEntity.ok(settlements);
    }
    
}
