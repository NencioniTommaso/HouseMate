package com.housemate.backend.controller;

import com.housemate.backend.service.expense.DebtService;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
@Validated
public class DebtController {

    private final DebtService debtService; 

    /**
     * Get debts for the current user's household filtered by transaction role and optional involved user.
     * HouseholdId is fetched automatically from user's current household.
     */
    @GetMapping
    public ResponseEntity<List<DebtResponseDTO>> getFilteredDebts(
            @Valid @ModelAttribute DebtFilterRequestDTO filter,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userIdString = userDetails.getUsername();
        UUID userId = UUID.fromString(userIdString);

        // Pass the parameter object to the service
        List<DebtResponseDTO> debts = debtService.getFilteredDebts(userId, filter);
        return ResponseEntity.ok(debts);
    }

    @DeleteMapping("/{debtId}")
    public ResponseEntity<Void> deleteDebt(@PathVariable UUID debtId) {
        debtService.deleteDebt(debtId);
        return ResponseEntity.noContent().build();
    }
}