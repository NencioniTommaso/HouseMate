package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    @Transactional
    public void addDebt(UUID debtorId, UUID creditorId, UUID householdId, BigDecimal amount) {
        if (debtorId.equals(creditorId)) return; // A user cannot owe themselves

        // 1. Fetch entities within the active transaction
        User debtor = userRepository.findById(debtorId)
                .orElseThrow(() -> new IllegalArgumentException("Debtor not found with ID: " + debtorId));
        User creditor = userRepository.findById(creditorId)
                .orElseThrow(() -> new IllegalArgumentException("Creditor not found with ID: " + creditorId));
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + householdId));

        // 2. Check if there's an inverse debt (Creditor already owes the Debtor)
        Debt inverseDebt = debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household).orElse(null);

        if (inverseDebt != null) {
            int comparison = inverseDebt.getAmount().compareTo(amount);
            
            if (comparison > 0) {
                // Creditor owes more than the new amount; reduce their debt
                inverseDebt.setAmount(inverseDebt.getAmount().subtract(amount));
                debtRepository.save(inverseDebt);
                return;
            } else if (comparison == 0) {
                // Amounts perfectly cancel out
                debtRepository.delete(inverseDebt);
                return;
            } else {
                // New debt is greater than the inverse debt. Wipe inverse debt and create new forward debt for the remainder.
                amount = amount.subtract(inverseDebt.getAmount());
                debtRepository.delete(inverseDebt);
            }
        }

        // 3. Add to existing debt or create a new one
        Debt existingDebt = debtRepository.findByDebtorAndCreditorAndHousehold(debtor, creditor, household).orElse(null);

        if (existingDebt != null) {
            existingDebt.setAmount(existingDebt.getAmount().add(amount));
            debtRepository.save(existingDebt);
        } else {
            Debt newDebt = new Debt(debtor, creditor, household, amount);
            debtRepository.save(newDebt);
        }
    }

    /**
     * Retrieves debts dynamically based on ANY combination of filter criteria.
     */
    @Transactional(readOnly = true)
    public List<DebtResponseDTO> getFilteredDebts(DebtFilterRequestDTO filter) {
        
        // 1. Convert the DTO into a dynamic database query
        Specification<Debt> spec = QuerySpecification.buildDebtFilter(filter);
        
        // 2. Execute the query and map the results to DTOs
        return debtRepository.findAll(spec).stream()
                .map(this::convertToDebtResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDebt(UUID debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + debtId));
        debtRepository.delete(debt);
    }

/**
     * Convert a Debt entity to a DebtResponseDTO.
     */
    private DebtResponseDTO convertToDebtResponseDTO(Debt debt) {
        return new DebtResponseDTO(
                debt.getId(),
                debt.getDebtor().getId(),
                getFullName(debt.getDebtor()),
                debt.getCreditor().getId(),
                getFullName(debt.getCreditor()),
                debt.getAmount()
        );
    }

    /**
     * Helper method to format user's full name safely.
     */
    private String getFullName(User user) {
        return user.getName() + " " + user.getSurname();
    }
}
