package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;

    /**
     * Updates the debt balance between two users. 
     * Applies debt simplification (netting out mutual debts).
     */
    @Transactional
    public void addDebt(User debtor, User creditor, BigDecimal amount) {
        if (debtor.equals(creditor)) return; // A user cannot owe themselves

        // Check if there's an inverse debt (Creditor already owes the Debtor)
        List<Debt> inverseDebts = debtRepository.findByDebtorAndCreditor(creditor, debtor);
        if (!inverseDebts.isEmpty()) {
            Debt inverseDebt = inverseDebts.get(0);
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
        if (!existingDebts.isEmpty()) {
            Debt existingDebt = existingDebts.get(0);
            existingDebt.setAmount(existingDebt.getAmount().add(amount));
            debtRepository.save(existingDebt);
        } else {
            Debt newDebt = new Debt(debtor, creditor, amount);
            debtRepository.save(newDebt);
        }
    }
}