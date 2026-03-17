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
import com.housemate.shared.enums.UserTransactionRole;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

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
    public void addDebt(
            @NonNull UUID debtorId,
            @NonNull UUID creditorId,
            @NonNull UUID householdId,
            @NonNull BigDecimal amount) {
        // 1. Fail-Fast Validation
        Assert.notNull(debtorId, "Debtor ID must not be null");
        Assert.notNull(creditorId, "Creditor ID must not be null");
        Assert.notNull(householdId, "Household ID must not be null");
        Assert.notNull(amount, "Amount must not be null");
        
        if (debtorId.equals(creditorId)) return; // A user cannot owe themselves

        // 2. Fetch entities within the active transaction
        User debtor = userRepository.findById(debtorId)
                .orElseThrow(() -> new IllegalArgumentException("Debtor not found with ID: " + debtorId));
        User creditor = userRepository.findById(creditorId)
                .orElseThrow(() -> new IllegalArgumentException("Creditor not found with ID: " + creditorId));
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + householdId));

        // 3. Check if there's an inverse debt (Creditor already owes the Debtor)
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

        // 4. Add to existing debt or create a new one
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
     * Retrieves debts dynamically based on filter criteria.
     * Fetches householdId from user's current household.
     */
    @Transactional(readOnly = true)
    public List<DebtResponseDTO> getFilteredDebts(
            @NonNull UUID userId,
            @NonNull DebtFilterRequestDTO filter) {
        // 1. Fail-Fast Validation
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(filter, "Filter DTO must not be null");
        
        // 2. Fetch user and extract householdId from their current household
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UUID householdId = null;
        if (user.getHouseholdMembership() != null && user.getHouseholdMembership().getHousehold() != null) {
            householdId = user.getHouseholdMembership().getHousehold().getId();
        }

        // 3. Convert the DTO into a dynamic database query
        Specification<Debt> spec = QuerySpecification.buildDebtFilter(userId, householdId, filter);
        
        // 4. Execute the query and map the results to DTOs
        return debtRepository.findAll(spec).stream()
                .map(debt -> convertToDebtResponseDTO(debt, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDebt(@NonNull UUID debtId) {
        // 1. Fail-Fast Validation
        Assert.notNull(debtId, "Debt ID must not be null");
        
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + debtId));
        debtRepository.delete(debt);
    }

    /**
     * Convert a Debt entity to a DebtResponseDTO.
     */
    private DebtResponseDTO convertToDebtResponseDTO(
            @NonNull Debt debt,
            @NonNull UUID userId) {
        // 1. Fail-Fast Validation
        Assert.notNull(debt, "Debt must not be null");
        Assert.notNull(userId, "User ID must not be null");
        
        UserTransactionRole userRole;
        UUID involvedId;
        String involvedName;

        if (debt.getDebtor().getId().equals(userId)) {
            // User is the debtor (owes money)
            userRole = UserTransactionRole.DEBTOR;
            involvedId = debt.getCreditor().getId();
            involvedName = getFullName(debt.getCreditor());
        } else if (debt.getCreditor().getId().equals(userId)) {
            // User is the creditor (is owed money)
            userRole = UserTransactionRole.CREDITOR;
            involvedId = debt.getDebtor().getId();
            involvedName = getFullName(debt.getDebtor());
        } else {
            throw new IllegalArgumentException("User is neither debtor nor creditor in this debt");
        }

        return new DebtResponseDTO(
                debt.getId(),
                userRole,
                involvedId,
                involvedName,
                debt.getAmount()
        );
    }

    /**
     * Helper method to format user's full name safely.
     */
    private String getFullName(@NonNull User user) {
        Assert.notNull(user, "User must not be null");
        return user.getName() + " " + user.getSurname();
    }
}
