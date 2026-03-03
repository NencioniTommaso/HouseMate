package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final DebtRepository debtRepository;

    @Transactional
    public Settlement settleDebt(Debt debt, User debtor, User creditor, BigDecimal amount) {
        // Fail-Fast validations
        if (amount.compareTo(debt.getAmount()) > 0) {
            throw new IllegalArgumentException("Cannot settle an amount greater than the existing debt.");
        }

        // Create the settlement record
        Settlement settlement = new Settlement(debt, debtor, creditor, amount);
        settlementRepository.save(settlement);

        // Decrease the debt or delete it if fully paid
        if (amount.compareTo(debt.getAmount()) == 0) {
            debtRepository.delete(debt);
        } else {
            debt.setAmount(debt.getAmount().subtract(amount));
            debtRepository.save(debt);
        }

        return settlement;
    }
}