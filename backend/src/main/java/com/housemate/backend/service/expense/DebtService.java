package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final HouseholdMembershipRepository householdMembershipRepository;

    /**
     * Updates the debt balance between two users in a household. 
     * Applies debt simplification (netting out mutual debts).
     */
    @Transactional
    public void addDebt(User debtor, User creditor, Household household, BigDecimal amount) {
        if (debtor.equals(creditor)) return; // A user cannot owe themselves

        // Check if there's an inverse debt (Creditor already owes the Debtor)
        List<Debt> inverseDebts = debtRepository.findByDebtorAndCreditor(creditor, debtor);
        Debt inverseDebt = inverseDebts.stream()
                .filter(d -> d.getHousehold().equals(household))
                .findFirst()
                .orElse(null);

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

        // Add to existing debt or create a new one
        List<Debt> existingDebts = debtRepository.findByDebtorAndCreditor(debtor, creditor);
        Debt existingDebt = existingDebts.stream()
                .filter(d -> d.getHousehold().equals(household))
                .findFirst()
                .orElse(null);

        if (existingDebt != null) {
            existingDebt.setAmount(existingDebt.getAmount().add(amount));
            debtRepository.save(existingDebt);
        } else {
            Debt newDebt = new Debt(debtor, creditor, household, amount);
            debtRepository.save(newDebt);
        }
    }

    /**
     * Get all debts in a household.
     *
     * @param household the household
     * @return list of all debts in the household
     */
    @Transactional(readOnly = true)
    public List<Debt> getDebtsInHousehold(Household household) {
        return debtRepository.findByHousehold(household);
    }

    /**
     * Get all debts owed by a user in a household.
     * If household is null, defaults to the user's current household.
     *
     * @param debtor    the user who owes
     * @param household the household (if null, uses user's current household)
     * @return list of debts owed by the user, or empty list if no household available
     */
    @Transactional(readOnly = true)
    public List<Debt> getDebtsOwnedByUserInHousehold(User debtor, Household household) {
        if (household == null) {
            Optional<HouseholdMembership> membership = householdMembershipRepository.findByUser(debtor)
                    .stream()
                    .findFirst();
            
            if (membership.isEmpty()) {
                return List.of();
            }
            household = membership.get().getHousehold();
        }

        return debtRepository.findByDebtorAndHousehold(debtor, household);
    }

    /**
     * Get all debts owed to a user in a household.
     * If household is null, defaults to the user's current household.
     *
     * @param creditor  the user who is owed
     * @param household the household (if null, uses user's current household)
     * @return list of debts owed to the user, or empty list if no household available
     */
    @Transactional(readOnly = true)
    public List<Debt> getDebtsOwnedToUserInHousehold(User creditor, Household household) {
        if (household == null) {
            Optional<HouseholdMembership> membership = householdMembershipRepository.findByUser(creditor)
                    .stream()
                    .findFirst();
            
            if (membership.isEmpty()) {
                return List.of();
            }
            household = membership.get().getHousehold();
        }

        return debtRepository.findByCreditorAndHousehold(creditor, household);
    }

    /**
     * Get all debts involving a user in a household (both as debtor and creditor).
     * If household is null, defaults to the user's current household.
     *
     * @param user      the user
     * @param household the household (if null, uses user's current household)
     * @return list of all debts involving the user, or empty list if no household available
     */
    @Transactional(readOnly = true)
    public List<Debt> getDebtsForUserInHousehold(User user, Household household) {
        if (household == null) {
            Optional<HouseholdMembership> membership = householdMembershipRepository.findByUser(user)
                    .stream()
                    .findFirst();
            
            if (membership.isEmpty()) {
                return List.of();
            }
            household = membership.get().getHousehold();
        }

        return debtRepository.findByUserAndHousehold(user, household);
    }
}